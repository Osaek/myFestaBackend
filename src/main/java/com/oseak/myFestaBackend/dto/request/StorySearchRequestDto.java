package com.oseak.myFestaBackend.dto.request;

import com.oseak.myFestaBackend.common.request.PageRequest;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "스토리 검색 요청")
public class StorySearchRequestDto extends PageRequest {
	//TODO: 멤버ID로 할 것인지, 한 번 더 의견나눠볼 것
	@Schema(description = "회원ID", example = "1")
	private Long memberId;

	@Schema(description = "검색 축제 키워드", example = "벚꽃축제")
	private String keyword;

	@Hidden
	@Schema(description = "축제ID")
	private Long festaId;
	
	@Schema(description = "내 비공개 스토리를 포함할지 여부(내 프로필 화면에서만 사용)")
	private Boolean includePrivateMine;

	@Schema(hidden = true)
	public boolean isIncludePrivateMine() {
		return Boolean.TRUE.equals(includePrivateMine);
	}
}

