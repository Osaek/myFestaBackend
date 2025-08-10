package com.oseak.myFestaBackend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.oseak.myFestaBackend.dto.request.FestivalSearchRequest;
import com.oseak.myFestaBackend.dto.response.FestivalSearchItem;
import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.entity.enums.FestaStatus;
import com.oseak.myFestaBackend.repository.FestaRepository;
import com.oseak.myFestaBackend.repository.FestivalSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestaService {

	private final FestaRepository festaRepository;
	private final WebClient webClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${tourapi.url}")
	private String baseUrl;

	@Value("${tourapi.service-key}")
	private String serviceKey;

	@Transactional
	public void fetchAndSaveFestivals(String eventStartDate, Integer areaCode) {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/searchFestival2")
				.queryParam("MobileOS", "ETC")
				.queryParam("MobileApp", UriUtils.encode("오색", StandardCharsets.UTF_8))
				.queryParam("_type", "json")
				.queryParam("eventStartDate", eventStartDate)
				.queryParam("numOfRows", 200)
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
				LocalDateTime startAt = parseDate(item.optString("eventstartdate"));
				LocalDateTime endAt = parseDate(item.optString("eventenddate"));
				Long contentId = item.optLong("contentid");
				Long contentTypeId = item.optLong("contenttypeid");

				FestaStatus status = getStatusByDate(startAt, endAt);
				Map<String, String> detailMap = fetchFestivalDetails(contentId, contentTypeId);
				Map<String, String> introMap = fetchFestivalIntro(contentId, contentTypeId);

				Optional<Festa> optionalFesta = festaRepository.findByContentId(contentId);
				if (optionalFesta.isPresent()) {
					log.info("기존 '{}' 행사 (contentId: {}) 업데이트 실행", title, contentId);
					Festa festa = optionalFesta.get();
					festa.updateContent(detailMap.get("overview"), detailMap.get("description"));
					festa.updateIntro(introMap.get("playtime"), introMap.get("usetimefestival"));
					festa.updateStatus(status);
					festaRepository.save(festa);
				} else {
					log.info("신규 '{}' 행사 (contentId: {}) 저장 실행", title, contentId);
					Festa festa = Festa.builder()
						.contentId(contentId)
						.festaName(title)
						.latitude(item.optDouble("mapy"))
						.longitude(item.optDouble("mapx"))
						.festaAddress(item.optString("addr1"))
						.festaStartAt(startAt)
						.festaEndAt(endAt)
						.areaCode(item.optInt("areacode"))
						.subAreaCode(item.optInt("sigungucode"))
						.imageUrl(item.optString("firstimage"))
						.openTime(introMap.get("playtime"))
						.feeInfo(introMap.get("usetimefestival"))
						.festaStatus(status)
						.overview(detailMap.get("overview"))
						.description(detailMap.get("description"))
						.build();
					festaRepository.save(festa);
				}
			}
		} catch (WebClientResponseException e) {
			throw new OsaekException(ServerErrorCode.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			throw new OsaekException(ServerErrorCode.UNKNOWN_SERVER_ERROR);
		}
	}

	private Map<String, String> fetchFestivalDetails(Long contentId, Long contentTypeId) {
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

	private Map<String, String> fetchFestivalIntro(Long contentId, Long contentTypeId) {
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

	private LocalDateTime parseDate(String dateStr) {
		try {
			return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
		} catch (Exception e) {
			return null;
		}
	}

	private FestaStatus getStatusByDate(LocalDateTime start, LocalDateTime end) {
		LocalDate today = LocalDate.now();
		if (start == null || end == null) {
			return FestaStatus.SCHEDULED;
		}
		if (today.isBefore(start.toLocalDate())) {
			return FestaStatus.SCHEDULED;
		} else if (!today.isAfter(end.toLocalDate())) {
			return FestaStatus.ONGOING;
		} else {
			return FestaStatus.COMPLETED;
		}
	}

	@Transactional
	public void updateAllFestaStatus() {
		LocalDate today = LocalDate.now();

		festaRepository.findAll().forEach(festa -> {
			LocalDate start = festa.getFestaStartAt() != null ? festa.getFestaStartAt().toLocalDate() : null;
			LocalDate end = festa.getFestaEndAt() != null ? festa.getFestaEndAt().toLocalDate() : null;

			FestaStatus newStatus;

			if (start == null || end == null) {
				newStatus = FestaStatus.SCHEDULED; // 기본값 또는 예외 처리 가능
			} else if (today.isBefore(start)) {
				newStatus = FestaStatus.SCHEDULED;
			} else if (!today.isAfter(end)) {
				newStatus = FestaStatus.ONGOING;
			} else {
				newStatus = FestaStatus.COMPLETED;
			}

			if (festa.getFestaStatus() != newStatus) {
				festa.updateStatus(newStatus);
			}
		});

		log.info("축제 진행상태 업데이트 완료");
	}

	/*TODO : festa 정보 저장될 때 festa_statistic도 같이 만들어줘야함.
		user_like좀 넣고 festa_statistic 도 넣고 해야 추천 API만들 듯.
	*/

	public List<FestaSimpleDto> findNearbyFesta(double latitude, double longitude, int distanceKm) {
		if (!List.of(1, 5, 10, 20).contains(distanceKm)) {
			throw new OsaekException(ServerErrorCode.MISSING_REQUIRED_FIELD);
		}

		return festaRepository.findByDistance(latitude, longitude, distanceKm).stream()
			.map(FestaSimpleDto::from)
			.toList();
	}

	public List<FestaSummaryDto> getFestaSummariesByContentIds(List<Long> contentIds) {
		List<Festa> festas = festaRepository.findAllByContentIdIn(contentIds);

		List<Long> foundIds = festas.stream()
			.map(Festa::getContentId)
			.toList();

		List<Long> missingIds = contentIds.stream()
			.filter(id -> !foundIds.contains(id))
			.toList();

		if (!missingIds.isEmpty()) {
			throw new OsaekException(ServerErrorCode.FESTA_NOT_FOUND);
		}

		return festas.stream()
			.map(FestaSummaryDto::from)
			.toList();
	}

	public List<FestaSimpleDto> getRandomFestivals(int count) {
		List<Festa> randomFestivals = festaRepository.findRandomFestivals(count);
		return randomFestivals.stream()
			.map(FestaSimpleDto::from)
			.toList();
	}

	public Page<FestivalSearchItem> search(FestivalSearchRequest request) {
		Specification<Festa> spec = FestivalSpecification.createSpecification(request);
		Pageable pageable = PageRequest.of(request.getValidPage(), request.getValidSize(),
			Sort.by(Sort.Direction.ASC, "festaStartAt"));

		Page<Festa> page = festaRepository.findAll(spec, pageable);
		return page.map(FestivalSearchItem::from);
	}

}
