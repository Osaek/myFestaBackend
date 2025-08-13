package com.oseak.myFestaBackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.service.StoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/story")
@Tag(name = "Story API", description = "스토리 관련 API(Controller)")
public class StoryController {

	private final StoryService storyService;

	@PostMapping("/upload")
	public ResponseEntity<CommonResponse<Void>> uploadStory(@RequestParam("files") List<MultipartFile> file) {
		storyService.uploadFiles(file);

		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null));
	}
}