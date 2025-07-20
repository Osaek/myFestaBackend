package com.oseak.myFestaBackend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.repository.FestaRepository;

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

				String status = getStatusByDate(startAt, endAt);
				Map<String, String> detailMap = fetchFestivalDetails(contentId, contentTypeId);

				Optional<Festa> optionalFesta = festaRepository.findByFestaNameAndFestaStartAt(title, startAt);
				if (optionalFesta.isPresent()) {
					Festa festa = optionalFesta.get();
					festa.updateContent(detailMap.get("overview"), detailMap.get("description"));
					festa.updateStatus(status);
					festaRepository.save(festa);
				} else {
					Festa festa = Festa.builder()
						.festaName(title)
						.latitude(item.optDouble("mapy"))
						.longitude(item.optDouble("mapx"))
						.festaAddress(item.optString("addr1"))
						.festaStartAt(startAt)
						.festaEndAt(endAt)
						.areaCode(item.optInt("areacode"))
						.subAreaCode(item.optInt("sigungucode"))
						.imageUrl(item.optString("firstimage"))
						.openTime(item.optString("playtime"))
						.feeInfo(item.optString("usetimefestival"))
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

	private LocalDateTime parseDate(String dateStr) {
		try {
			return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
		} catch (Exception e) {
			return null;
		}
	}

	private String getStatusByDate(LocalDateTime start, LocalDateTime end) {
		LocalDate today = LocalDate.now();
		if (start == null || end == null) {
			return "false";
		}
		return (!today.isBefore(start.toLocalDate()) && !today.isAfter(end.toLocalDate())) ? "true" : "false";
	}
}
