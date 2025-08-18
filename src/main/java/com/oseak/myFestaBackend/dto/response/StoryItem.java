package com.oseak.myFestaBackend.dto.response;

import java.time.LocalDateTime;

import com.oseak.myFestaBackend.common.util.ShortCodeUtil;
import com.oseak.myFestaBackend.entity.Story;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryItem {
	private String storyCode;
	private Long memberId;
	private Boolean isOpen;
	private String thumbnailUrl;
	private String festaName;
	private String processingStatus;
	private String storyS3Url;
	private LocalDateTime createdAt;

	public static StoryItem from(Story story) {
		return StoryItem.builder()
			.storyCode(ShortCodeUtil.encode(story.getStoryId()))
			.memberId(story.getMemberId())
			.isOpen(story.getIsOpen())
			.thumbnailUrl(story.getThumbnailUrl())
			.storyS3Url(story.getStoryS3Url())
			.festaName(story.getFestaName())
			.createdAt(story.getCreatedAt())
			.processingStatus(story.getProcessingStatus())
			.build();
	}
}
