package com.oseak.myFestaBackend.entity.enums;

public enum ProcessingStatus {
	PROCESSING("처리중"),
	COMPLETED("완료"),
	FAILED("실패");

	private final String description;

	ProcessingStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}