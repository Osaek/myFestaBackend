package com.oseak.myFestaBackend.facade;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryUploadRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryVisibilityUpdateRequestDto;
import com.oseak.myFestaBackend.dto.response.StoryItem;
import com.oseak.myFestaBackend.entity.Story;
import com.oseak.myFestaBackend.service.MemberService;
import com.oseak.myFestaBackend.service.StoryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoryFacade {

	private final StoryService storyService;
	private final MemberService memberService;

	public Page<StoryItem> searchStories(StorySearchRequestDto request, Long viewerMemberId) {
		Page<Story> storyPage = storyService.searchStories(request, viewerMemberId);
		return storyPage.map(story -> {
			String nickname = memberService.getNicknameByMemberId(story.getMemberId());
			return StoryItem.from(story, nickname);
		});
	}

	public StoryItem getStory(String storyCode, Long requesterMemberId) {
		Story story = storyService.getStoryEntity(storyCode, requesterMemberId);
		String nickname = memberService.getNicknameByMemberId(story.getMemberId());
		return StoryItem.from(story, nickname);
	}

	public StoryItem updateStoryVisibility(StoryVisibilityUpdateRequestDto request, Long requesterId) {
		Story story = storyService.updateStoryVisibilityEntity(request, requesterId);
		String nickname = memberService.getNicknameByMemberId(story.getMemberId());
		return StoryItem.from(story, nickname);
	}

	public void softDeleteStory(String storyCode, Long requesterMemberId) {
		storyService.softDeleteStory(storyCode, requesterMemberId);
	}

	public void deleteStory() {
		storyService.deleteStory();
	}

	public Story uploadStory(MultipartFile file, Long memberId) {
		return storyService.uploadStory(file, memberId);
	}

	public StoryItem uploadStoryAsync(StoryUploadRequestDto requestDto, Long memberId) {
		Story story = storyService.uploadStoryAsyncEntity(requestDto, memberId);
		String nickname = memberService.getNicknameByMemberId(story.getMemberId());
		return StoryItem.from(story, nickname);
	}
}
