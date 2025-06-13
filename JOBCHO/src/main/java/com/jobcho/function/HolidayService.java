package com.jobcho.function;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HolidayService {

	private final String SERVICE_KEY = "l8KmJjMsc1p0RAUcmQfyIDstSciQ7G4rLm9DeOHK9kmWv5pcqHlm8RjMEnvdCvCUe/5uVVChDGWvHRpLSVhHSA==";

	public List<HolidayDto> fetchHolidays(int year, int month) {
		List<HolidayDto> result = new ArrayList<>();
		RestTemplate restTemplate = new RestTemplate();

		String[] endpoints = { "getRestDeInfo", "getSubsttHolidayInfo" };

		for (String endpoint : endpoints) {
			String url = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/" + endpoint + "?solYear="
					+ year + "&solMonth=" + String.format("%02d", month) + "&ServiceKey=" + SERVICE_KEY + "&_type=json";

			log.info("API 호출: {}", url);

			try {
				ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
				log.info("응답 내용: {}", response.getBody());

				JSONObject json = new JSONObject(response.getBody());
				JSONObject body = json.getJSONObject("response").getJSONObject("body");

				if (body.has("items")) {
					Object itemsRaw = body.get("items");

					if (itemsRaw instanceof JSONObject items) {
						if (items.has("item")) {
							Object itemObj = items.get("item");
							JSONArray itemArray = itemObj instanceof JSONArray ? (JSONArray) itemObj
									: new JSONArray().put(itemObj);

							for (int i = 0; i < itemArray.length(); i++) {
								JSONObject item = itemArray.getJSONObject(i);
								String dateName = item.getString("dateName");
								String locdate = item.get("locdate").toString();
								result.add(new HolidayDto(dateName, locdate));
							}
						}
					} else {
						log.info("공휴일 없음: items가 빈 문자열 또는 없음");
					}
				}
			} // 오류때문에 넣은것 지워도 실행에 문제는 X
			  catch (org.springframework.web.client.HttpServerErrorException e) {
				if (e.getStatusCode().value() == 500 && endpoint.equals("getSubsttHolidayInfo")) {
					log.warn(endpoint + " 서버 에러 발생 (무시하고 계속 진행): {}", e.getMessage());
				} else {
					log.error(endpoint + " API 서버 에러", e);
				}
			} catch (Exception e) {
				log.error(endpoint + " API 호출 또는 파싱 실패", e);
			}
		}
		return result;
	}

}
