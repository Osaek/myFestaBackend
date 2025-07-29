package com.oseak.myFestaBackend.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("정적 프로필 이미지 요청이 200 OK로 응답되어야 하며, Cache-Control 헤더를 포함한다")
	void testStaticProfileImageResponse() throws Exception {
		var result = mockMvc.perform(get("/images/profiles/profile01.svg"))
			.andExpect(status().isOk())
			.andReturn();

		String cacheControl = result.getResponse().getHeader("Cache-Control");

		assertThat(cacheControl).isNotNull();
		assertThat(cacheControl).contains("max-age=31536000");
		assertThat(cacheControl).contains("public");
	}
}
