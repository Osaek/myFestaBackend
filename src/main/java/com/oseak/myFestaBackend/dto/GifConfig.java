package com.oseak.myFestaBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GifConfig {
	private int width;
	private int height;
	private int fps;
	private String filter;
}
