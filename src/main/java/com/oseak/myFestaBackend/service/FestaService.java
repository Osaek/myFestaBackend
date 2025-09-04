package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ServerErrorCode.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.dto.FestaSimpleDto;
import com.oseak.myFestaBackend.dto.FestaSummaryDto;
import com.oseak.myFestaBackend.dto.request.FestaNearRequestDto;
import com.oseak.myFestaBackend.dto.response.FestaDetailResponseDto;
import com.oseak.myFestaBackend.dto.search.FestaSearchItemDto;
import com.oseak.myFestaBackend.dto.search.FestaSearchRequestDto;
import com.oseak.myFestaBackend.entity.DevPickFesta;
import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.entity.FestaStatistic;
import com.oseak.myFestaBackend.entity.enums.FestaStatus;
import com.oseak.myFestaBackend.repository.DevPickFestaRepository;
import com.oseak.myFestaBackend.repository.FestaRepository;
import com.oseak.myFestaBackend.repository.FestaSpecification;
import com.oseak.myFestaBackend.repository.FestaStatisticRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestaService {

	private final FestaRepository festaRepository;
	private final DevPickFestaRepository devPickFestaRepository;
	private final WebClient webClient;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FestaStatisticRepository festaStatisticRepository;

	@Value("${tourapi.url}")
	private String baseUrl;

	@Value("${tourapi.service-key}")
	private String serviceKey;

	@Transactional
	public void fetchAndSaveFestas(String eventStartDate, Integer areaCode) {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/searchFestival2")
				.queryParam("MobileOS", "ETC")
				.queryParam("MobileApp", UriUtils.encode("오색", StandardCharsets.UTF_8))
				.queryParam("_type", "json")
				.queryParam("eventStartDate", eventStartDate)
				.queryParam("numOfRows", 1000)
				.queryParam("serviceKey", serviceKey)
				.queryParamIfPresent("areaCode", Optional.ofNullable(areaCode))
				.build(true)
				.toUri();

			String jsonResponse = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			JSONArray items = new JSONObject(jsonResponse)
				.getJSONObject("response")
				.getJSONObject("body")
				.getJSONObject("items")
				.optJSONArray("item");

			if (items == null) {
				log.info("가져온 축제 데이터가 없습니다. (eventStartDate={}, areaCode={})", eventStartDate, areaCode);
				log.info("Festival list response: {}", jsonResponse);
				return;
			}

			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				String title = item.optString("title");
				Long festaId = item.optLong("contentid");

				try {
					LocalDate startAt = parseDate(item.optString("eventstartdate"));
					LocalDate endAt = parseDate(item.optString("eventenddate"));

					Long contentTypeId = item.optLong("contenttypeid");

					FestaStatus status = getStatusByDate(startAt, endAt);
					Map<String, String> detailMap = fetchFestaDetails(festaId, contentTypeId);
					Map<String, String> introMap = fetchFestaIntro(festaId, contentTypeId);

					Optional<Festa> optionalFesta = festaRepository.findById(festaId);
					if (optionalFesta.isPresent()) {
						log.info("기존 '{}' 행사 (festaId: {}) 업데이트 실행", title, festaId);
						Festa festa = optionalFesta.get();
						festa.updateContent(brToNewLine(detailMap.get("overview")),
							brToNewLine(detailMap.get("description")));
						festa.updateIntro(brToNewLine(introMap.get("playtime")),
							brToNewLine(introMap.get("usetimefestival")));
						festa.updateStatus(status);
						festaRepository.save(festa);
					} else {
						log.info("신규 '{}' 행사 (festaId: {}) 저장 실행", title, festaId);
						Festa festa = Festa.builder()
							.festaId(festaId)
							.festaName(title)
							.latitude(item.optDouble("mapy"))
							.longitude(item.optDouble("mapx"))
							.festaAddress(item.optString("addr1"))
							.festaStartAt(startAt)
							.festaEndAt(endAt)
							.areaCode(item.optInt("areacode"))
							.subAreaCode(item.optInt("sigungucode"))
							.imageUrl(toHttps(item.optString("firstimage")))
							.openTime(brToNewLine(introMap.get("playtime")))
							.feeInfo(brToNewLine(introMap.get("usetimefestival")))
							.festaStatus(status)
							.overview(brToNewLine(detailMap.get("overview")))
							.description(brToNewLine(detailMap.get("description")))
							.build();
						festaRepository.save(festa);
					}
					getOrCreateFestaStatistic(festaId);
				} catch (Exception exception) {
					// 해당 건만 스킵하고 계속 진행
					log.warn("축제 처리 실패 - festaId={}, title='{}', 원인={}", festaId, title, exception.toString(),
						exception);
				}
			}
		} catch (WebClientResponseException e) {
			log.warn("searchFestival2 호출 실패 (areaCode={}, status={}, body={})",
				areaCode, e.getStatusCode(), e.getResponseBodyAsString());
		} catch (Exception e) {
			log.warn("축제 목록 수집 실패 (areaCode={}): {}", areaCode, e.getMessage(), e);
		}
	}

	private Map<String, String> fetchFestaDetails(Long contentId, Long contentTypeId) {
		Map<String, String> result = new HashMap<>();
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/detailInfo2")
				.queryParam("MobileOS", "ETC")
				.queryParam("MobileApp", UriUtils.encode("오색", StandardCharsets.UTF_8))
				.queryParam("_type", "json")
				.queryParam("contentId", contentId)
				.queryParam("contentTypeId", contentTypeId)
				.queryParam("serviceKey", serviceKey)
				.build(true)
				.toUri();

			String response = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			JSONArray infos = new JSONObject(response)
				.getJSONObject("response")
				.getJSONObject("body")
				.getJSONObject("items")
				.optJSONArray("item");

			if (infos != null) {
				for (int i = 0; i < infos.length(); i++) {
					JSONObject info = infos.getJSONObject(i);
					Map<String, String> parsed = objectMapper.readValue(info.toString(),
						new TypeReference<>() {
						});
					String name = parsed.get("infoname");
					String text = parsed.get("infotext");
					if ("행사소개".equals(name)) {
						result.put("overview", text);
					} else if ("행사내용".equals(name)) {
						result.put("description", text);
					}
				}
			}
		} catch (WebClientResponseException e) {
			throw new OsaekException(ServerErrorCode.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			throw new OsaekException(ServerErrorCode.MALFORMED_RESPONSE);
		}
		return result;
	}

	private Map<String, String> fetchFestaIntro(Long contentId, Long contentTypeId) {
		Map<String, String> result = new HashMap<>();
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/detailIntro2")
				.queryParam("MobileOS", "ETC")
				.queryParam("MobileApp", UriUtils.encode("오색", StandardCharsets.UTF_8))
				.queryParam("_type", "json")
				.queryParam("contentId", contentId)
				.queryParam("contentTypeId", contentTypeId)
				.queryParam("serviceKey", serviceKey)
				.build(true)
				.toUri();

			String response = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			JSONArray items = new JSONObject(response)
				.getJSONObject("response")
				.getJSONObject("body")
				.getJSONObject("items")
				.optJSONArray("item");

			if (items != null && items.length() > 0) {
				JSONObject intro = items.getJSONObject(0);
				String playtime = optStringOrNull(intro, "playtime");
				String fee = optStringOrNull(intro, "usetimefestival");
				result.put("playtime", playtime);
				result.put("usetimefestival", fee);
			}
		} catch (WebClientResponseException e) {
			throw new OsaekException(ServerErrorCode.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			throw new OsaekException(ServerErrorCode.MALFORMED_RESPONSE);
		}
		return result;
	}

	private String optStringOrNull(JSONObject o, String key) {
		String v = o.optString(key, null);
		return (v == null || v.isBlank()) ? null : v;
	}

	private LocalDate parseDate(String dateStr) {
		try {
			return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (Exception e) {
			return null;
		}
	}

	private FestaStatus getStatusByDate(LocalDate start, LocalDate end) {
		LocalDate today = LocalDate.now();
		if (start == null || end == null) {
			return FestaStatus.SCHEDULED;
		}
		if (today.isBefore(start)) {
			return FestaStatus.SCHEDULED;
		} else if (!today.isAfter(end)) {
			return FestaStatus.ONGOING;
		} else {
			return FestaStatus.COMPLETED;
		}
	}

	@Transactional
	public void updateAllFestaStatus() {

		festaRepository.findAll().forEach(festa -> {
			LocalDate start = festa.getFestaStartAt();
			LocalDate end = festa.getFestaEndAt();

			FestaStatus newStatus = getStatusByDate(start, end);

			if (festa.getFestaStatus() != newStatus) {
				festa.updateStatus(newStatus);
			}
		});

		log.info("축제 진행상태 업데이트 완료");
	}

	public Page<FestaSimpleDto> findNearbyFesta(FestaNearRequestDto req) {
		if (req.getLatitude() == null || req.getLongitude() == null) {
			throw new OsaekException(ServerErrorCode.MISSING_REQUIRED_FIELD);
		}
		Pageable pageable = PageRequest.of(req.getValidPage(), req.getValidSize());
		Page<Festa> page = festaRepository.findByDistance(
			req.getLatitude(),
			req.getLongitude(),
			req.getValidDistanceKm(),
			pageable
		);
		return page.map(FestaSimpleDto::from);
	}

	public List<FestaSummaryDto> getFestaSummariesByFestaIds(List<Long> festaIds) {
		List<Festa> festas = festaRepository.findAllByFestaIdIn(festaIds);

		List<Long> foundIds = festas.stream()
			.map(Festa::getFestaId)
			.toList();

		List<Long> missingIds = festaIds.stream()
			.filter(id -> !foundIds.contains(id))
			.toList();

		if (!missingIds.isEmpty()) {
			throw new OsaekException(ServerErrorCode.FESTA_NOT_FOUND);
		}

		return festas.stream()
			.map(FestaSummaryDto::from)
			.toList();
	}

	public List<FestaSimpleDto> getRandomFestas(int count) {
		List<Festa> randomFestas = festaRepository.findRandomFestas(count);
		return randomFestas.stream()
			.map(FestaSimpleDto::from)
			.toList();
	}

	public Page<FestaSearchItemDto> search(FestaSearchRequestDto request) {
		Specification<Festa> spec = FestaSpecification.createSpecification(request);
		Pageable pageable = PageRequest.of(request.getValidPage(), request.getValidSize(),
			Sort.by(Sort.Direction.ASC, "festaStartAt"));

		Page<Festa> page = festaRepository.findAll(spec, pageable);
		return page.map(FestaSearchItemDto::from);
	}

	public List<DevPickFesta> getDeveloperPicks(int count) {
		return devPickFestaRepository.pickRandom(count);
	}

	/**
	 * 축제 상세 정보 조회
	 *
	 * @param id 축제 ID
	 * @return FestaDetailResponseDto 축제 상세 정보
	 */
	public FestaDetailResponseDto getDetail(Long id) {
		log.debug("축제 상세 정보 조회 시작: id={}", id);

		Festa festa = festaRepository.findById(id)
			.orElseThrow(() -> {
				log.debug("축제를 찾을 수 없습니다: id={}", id);
				return new OsaekException(FESTA_NOT_FOUND);
			});

		FestaDetailResponseDto responseDto = FestaDetailResponseDto.from(festa);

		log.debug("축제 상세 정보 조회 완료: id={}, name={}", id, festa.getFestaName());
		return responseDto;
	}

	private FestaStatistic getOrCreateFestaStatistic(Long festaId) {
		return festaStatisticRepository.findById(festaId)
			.orElseGet(() -> festaStatisticRepository.save(
				FestaStatistic.builder().festaId(festaId).build()
			));
	}

	private String toHttps(String url) {
		if (url == null) {
			return null;
		}
		String u = url.trim();
		if (u.isEmpty()) {
			return null;
		}
		if (u.startsWith("//")) {
			return "https:" + u;
		}
		return u.replaceFirst("^http://", "https://");
	}

	private String brToNewLine(String s) {
		if (s == null) {
			return null;
		}
		return s.replaceAll("(?i)<br\\s*/?>", "\n");
	}

}
