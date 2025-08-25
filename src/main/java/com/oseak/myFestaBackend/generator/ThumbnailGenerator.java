package com.oseak.myFestaBackend.generator;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.dto.GifConfig;
import com.oseak.myFestaBackend.dto.ImageSize;
import com.oseak.myFestaBackend.dto.Thumbnail;
import com.oseak.myFestaBackend.dto.ThumbnailResult;
import com.oseak.myFestaBackend.entity.enums.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ThumbnailGenerator {

	private String tempDir = "/tmp/thumbnails";

	@Value("${app.media.max-original-size:50485760}") // 50MB 기본값
	private long maxOriginalSize;

	@Value("${app.media.compression-quality:85}")
	private int compressionQuality;

	// MultipartFile을 받아서 썸네일 생성 (압축 포함)
	public ThumbnailResult generateThumbnails(MultipartFile file, String mediaId, MediaType mediaType) {
		String tempFilePath = null;
		String compressedFilePath = null;

		try {
			// 1. MultipartFile을 임시 파일로 저장
			tempFilePath = saveMultipartFileToTemp(file, mediaId);

			// 2. 파일 크기 확인 및 필요시 압축
			compressedFilePath = compressOriginalIfNeeded(tempFilePath, file.getSize(), mediaId, mediaType);
			String finalFilePath = compressedFilePath != null ? compressedFilePath : tempFilePath;

			// 3. 썸네일 생성
			ThumbnailResult result = switch (mediaType) {
				case IMAGE -> generateImageThumbnails(finalFilePath, mediaId);
				case VIDEO -> generateVideoThumbnails(finalFilePath, mediaId);
			};

			result.setOriginalLocalPath(finalFilePath);

			// 4. 압축된 파일 정보 설정
			if (compressedFilePath != null) {
				new File(tempFilePath).delete();
			}

			return result;

		} catch (Exception e) {
			log.error("Failed to generate thumbnails for mediaId: {}", mediaId, e);

			// 에러 발생시에만 임시 파일 정리
			if (tempFilePath != null) {
				new File(tempFilePath).delete();
			}
			if (compressedFilePath != null && !compressedFilePath.equals(tempFilePath)) {
				new File(compressedFilePath).delete();
			}

			throw e;
		}
	}

	// MultipartFile을 임시 파일로 저장
	private String saveMultipartFileToTemp(MultipartFile file, String mediaId) {
		try {
			log.info("Using tempDir: {}", tempDir); // 디버그 로그 추가

			// 임시 디렉토리 생성
			File tempDirectory = new File(tempDir);
			log.info("Temp directory path: {}, exists: {}", tempDirectory.getAbsolutePath(), tempDirectory.exists());

			if (!tempDirectory.exists()) {
				boolean created = tempDirectory.mkdirs();
				log.info("Created temp directory: {}, success: {}", tempDirectory.getAbsolutePath(), created);
			}

			// 파일 확장자 추출
			String originalFilename = file.getOriginalFilename();
			String extension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// 임시 파일 경로 생성
			String tempFilePath = tempDir + "/" + mediaId + "_original" + extension;
			File tempFile = new File(tempFilePath);

			// MultipartFile을 임시 파일로 저장
			file.transferTo(tempFile);

			log.debug("Saved multipart file to temp: {}", tempFilePath);
			return tempFilePath;

		} catch (IOException e) {
			log.error("Failed to save multipart file to temp for mediaId: {}", mediaId, e);
			throw new OsaekException(TEMPFILE_CANT_CREATE);
		}
	}

	// === 이미지 썸네일 생성 ===

	private ThumbnailResult generateImageThumbnails(String localImagePath, String mediaId) {
		List<Thumbnail> thumbnails = new ArrayList<>();

		Map<String, ImageSize> sizes = Map.of(
			"small", new ImageSize(150, 150),
			"medium", new ImageSize(300, 300),
			"large", new ImageSize(600, 600)
		);

		for (Map.Entry<String, ImageSize> entry : sizes.entrySet()) {
			String sizeKey = entry.getKey();
			ImageSize size = entry.getValue();

			String localThumbnailPath = generateImageThumbnail(
				localImagePath, mediaId + "_" + sizeKey, size.getWidth(), size.getHeight());

			thumbnails.add(Thumbnail.builder()
				.size(sizeKey)
				.localPath(localThumbnailPath)
				.format("jpg")
				.width(size.getWidth())
				.height(size.getHeight())
				.build());
		}

		return ThumbnailResult.builder()
			.mediaType(MediaType.IMAGE)
			.thumbnails(thumbnails)
			.build();
	}

	public String generateImageThumbnail(String inputPath, String outputName, int width, int height) {
		String outputPath = tempDir + "/" + outputName + ".jpg";

		String[] command = {
			"ffmpeg", "-i", inputPath,
			"-vf", String.format("scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d",
			width, height, width, height),
			"-q:v", "2",
			"-y", outputPath
		};

		executeFFmpeg(command);
		return outputPath;
	}

	// === 비디오 썸네일 생성 ===

	private ThumbnailResult generateVideoThumbnails(String localVideoPath, String mediaId) {
		List<Thumbnail> thumbnails = new ArrayList<>();

		try {
			log.info("Generating static thumbnail for video: {}", mediaId);

			// 여러 사이즈의 정적 썸네일 생성
			Map<String, ImageSize> sizes = Map.of(
				"small", new ImageSize(150, 150),
				"medium", new ImageSize(300, 300),
				"large", new ImageSize(600, 600)
			);

			for (Map.Entry<String, ImageSize> entry : sizes.entrySet()) {
				String sizeKey = entry.getKey();
				ImageSize size = entry.getValue();

				String thumbnailPath = generateVideoStaticThumbnail(
					localVideoPath, mediaId + "_" + sizeKey, size.getWidth(), size.getHeight());

				thumbnails.add(Thumbnail.builder()
					.size(sizeKey)
					.localPath(thumbnailPath)
					.format("jpg")
					.width(size.getWidth())
					.height(size.getHeight())
					.build());
			}

			log.info("Successfully generated {} static thumbnails for video: {}", thumbnails.size(), mediaId);

			return ThumbnailResult.builder()
				.mediaType(MediaType.VIDEO)
				.thumbnails(thumbnails)
				.staticThumbnailS3Url(null)  // 메인 정적 썸네일
				.build();

		} catch (Exception e) {
			log.error("Failed to generate video thumbnails for mediaId: {}", mediaId, e);
			throw new OsaekException(THUMBNAIL_CANT_CREATE);
		}
	}

	private String generateVideoStaticThumbnail(String videoPath, String outputName, int width, int height) {
		String outputPath = tempDir + "/" + outputName + ".jpg";

		// 비디오에서 첫 번째 프레임을 JPG로 추출하는 간단하고 안정적인 명령어
		String[] command = {
			"ffmpeg", "-i", videoPath,
			"-vframes", "1",  // 첫 번째 프레임만
			"-ss", "0",       // 1초 지점에서 (첫 프레임이 검은 화면일 수 있어서)
			"-vf",
			String.format("scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2:color=white",
				width, height, width, height),
			"-q:v", "2",      // 고품질
			"-y", outputPath
		};

		log.info("Generating static thumbnail: {} -> {}", videoPath, outputPath);
		executeFFmpeg(command);

		// 생성된 파일 검증
		File outputFile = new File(outputPath);
		if (!outputFile.exists() || outputFile.length() == 0) {
			log.error("Static thumbnail not created or empty: {}", outputPath);
			throw new OsaekException(THUMBNAIL_CANT_CREATE);
		}

		log.info("Static thumbnail created successfully: {} (size: {} bytes)", outputPath, outputFile.length());
		return outputPath;
	}

	// 비디오 파일 유효성 검사
	private boolean isValidVideoFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || file.length() == 0) {
			return false;
		}

		// 비디오 정보 확인을 위한 간단한 FFprobe 명령어
		try {
			String[] command = {"ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", filePath};
			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();
			int exitCode = process.waitFor();
			return exitCode == 0;
		} catch (Exception e) {
			log.warn("Failed to validate video file: {}", filePath, e);
			return false;
		}
	}

	// public String generateVideoGifThumbnail(String videoPath, String outputName,
	// 	GifConfig config, int startSeconds) {
	// 	String outputPath = tempDir + "/" + outputName + ".gif";
	// 	String paletteFile = tempDir + "/" + outputName + "_palette.png";
	//
	// 	try {
	// 		// 1. 팔레트 생성
	// 		log.info("Creating palette for GIF: {}", paletteFile);
	// 		String[] paletteCommand = {
	// 			"ffmpeg", "-i", videoPath,
	// 			"-ss", String.valueOf(startSeconds),
	// 			"-t", String.valueOf(config.getDuration()),
	// 			"-vf", String.format("fps=%d,scale=%d:%d:flags=lanczos,palettegen",
	// 			config.getFps(), config.getWidth(), config.getHeight()),
	// 			"-y", paletteFile
	// 		};
	// 		executeFFmpeg(paletteCommand);
	//
	// 		// 2. 팔레트 파일 존재 확인
	// 		File palette = new File(paletteFile);
	// 		if (!palette.exists() || palette.length() == 0) {
	// 			log.error("Palette file not created or empty: {}", paletteFile);
	// 			throw new OsaekException(THUMBNAIL_CANT_CREATE);
	// 		}
	// 		log.info("Palette created successfully: {} (size: {} bytes)", paletteFile, palette.length());
	//
	// 		// 3. GIF 생성
	// 		log.info("Creating GIF: {}", outputPath);
	// 		String[] gifCommand = {
	// 			"ffmpeg", "-i", videoPath, "-i", paletteFile,
	// 			"-ss", String.valueOf(startSeconds),
	// 			"-t", String.valueOf(config.getDuration()),
	// 			"-lavfi", String.format("fps=%d,scale=%d:%d:flags=lanczos[x];[x][1:v]paletteuse",
	// 			config.getFps(), config.getWidth(), config.getHeight()),
	// 			"-y", outputPath
	// 		};
	// 		executeFFmpeg(gifCommand);
	//
	// 		// 4. GIF 파일 존재 확인
	// 		File gifFile = new File(outputPath);
	// 		if (!gifFile.exists() || gifFile.length() == 0) {
	// 			log.error("GIF file not created or empty: {}", outputPath);
	// 			throw new OsaekException(THUMBNAIL_CANT_CREATE);
	// 		}
	// 		log.info("GIF created successfully: {} (size: {} bytes)", outputPath, gifFile.length());
	//
	// 		return outputPath;
	//
	// 	} catch (Exception e) {
	// 		log.error("Failed to generate GIF thumbnail: {}", outputName, e);
	// 		throw new OsaekException(THUMBNAIL_CANT_CREATE);
	// 	}
	// }

	// === 파일 압축 기능 ===

	/**
	 * 원본 파일이 크기 제한을 초과하면 압축
	 */
	private String compressOriginalIfNeeded(String originalPath, long fileSize, String mediaId, MediaType mediaType) {
		if (fileSize <= maxOriginalSize) {
			log.debug("File size {} is within limit {}, no compression needed", fileSize, maxOriginalSize);
			return null; // 압축 불필요
		}

		log.info("File size {} exceeds limit {}, compressing...", fileSize, maxOriginalSize);

		return switch (mediaType) {
			case IMAGE -> compressImage(originalPath, mediaId);
			case VIDEO -> compressVideo(originalPath, mediaId);
		};
	}

	/**
	 * 이미지 압축
	 */
	private String compressImage(String inputPath, String mediaId) {
		String outputPath = tempDir + "/" + mediaId + "_compressed.jpg";

		String[] command = {
			"ffmpeg", "-i", inputPath,
			"-q:v", String.valueOf(compressionQuality), // 품질 설정 (1-31, 낮을수록 높은 품질)
			"-y", outputPath
		};

		executeFFmpeg(command);

		File compressedFile = new File(outputPath);
		long compressedSize = compressedFile.length();
		log.info("Image compressed: {} bytes -> {} bytes", new File(inputPath).length(), compressedSize);

		return outputPath;
	}

	/**
	 * 비디오 압축
	 */
	private String compressVideo(String inputPath, String mediaId) {
		String outputPath = tempDir + "/" + mediaId + "_compressed.mp4";

		String[] command = {
			"ffmpeg", "-i", inputPath,

			// 가장 호환성 높은 설정
			"-c:v", "libx264",
			"-profile:v", "high",            // 높은 호환성
			"-level", "4.1",                 // 대부분 디바이스 지원
			"-pix_fmt", "yuv420p",           // 8-bit 강제 변환

			// 압축 설정
			"-crf", "26",                    // 적당한 품질
			"-preset", "medium",
			"-tune", "film",

			// 해상도/프레임레이트 정규화
			"-vf",
			"scale='min(1920,iw)':'min(1080,ih)':force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2:black",
			"-r", "30",
			"-g", "60",                      // GOP 설정

			// 오디오 정규화
			"-c:a", "aac",
			"-b:a", "128k",
			"-ar", "48000",
			"-ac", "2",

			// 앱 재생 최적화
			"-movflags", "+faststart+frag_keyframe",
			"-f", "mp4",
			"-avoid_negative_ts", "make_zero",

			"-y", outputPath
		};

		executeFFmpeg(command);
		return outputPath;
	}

	public String convertVideoToGif(String videoPath, String outputName, int maxDuration) {
		try {
			// 1. 비디오 길이 확인
			double videoDuration = getVideoDuration(videoPath);
			log.info("Video duration: {} seconds", videoDuration);

			// 2. GIF 생성
			if (videoDuration <= maxDuration) {
				// 5초 이하면 전체 영상 사용
				return createGifFromFullVideo(videoPath, outputName);
			} else {
				// 5초 이상이면 처음 5초만 사용
				return createGifFromVideoSegment(videoPath, outputName, maxDuration);
			}

		} catch (Exception e) {
			log.error("Failed to convert video to GIF: {}", videoPath, e);
			throw new RuntimeException("GIF conversion failed", e);
		}
	}

	/**
	 * 비디오 전체를 GIF로 변환 (5초 이하)
	 */
	private String createGifFromFullVideo(String videoPath, String outputName) {
		String outputPath = tempDir + "/" + outputName + ".gif";

		String[] command = {
			"ffmpeg", "-i", videoPath,

			// 해상도 최적화 (GIF는 용량이 크므로)
			"-vf",
			"fps=15,scale=480:320:flags=lanczos:force_original_aspect_ratio=decrease,pad=480:320:(ow-iw)/2:(oh-ih)/2",

			// GIF 최적화
			"-loop", "0",              // 무한 반복
			"-y", outputPath
		};

		executeFFmpeg(command);
		log.info("Created full GIF: {}", outputPath);
		return outputPath;
	}

	/**
	 * 비디오의 처음 N초를 GIF로 변환
	 */
	private String createGifFromVideoSegment(String videoPath, String outputName, int duration) {
		String outputPath = tempDir + "/" + outputName + ".gif";

		String[] command = {
			"ffmpeg", "-i", videoPath,
			"-t", String.valueOf(duration),  // 처음 N초만
			"-vf",
			"fps=15,scale=480:320:flags=lanczos:force_original_aspect_ratio=decrease,pad=480:320:(ow-iw)/2:(oh-ih)/2",
			"-loop", "0",
			"-y", outputPath
		};

		executeFFmpeg(command);
		log.info("Created {}-second GIF: {}", duration, outputPath);
		return outputPath;
	}

	/**
	 * 고품질 GIF 생성 (2단계 팔레트 방식)
	 */
	public String createHighQualityGif(String videoPath, String outputName, int maxDuration) {
		try {
			double videoDuration = getVideoDuration(videoPath);
			int actualDuration = (int)Math.min(videoDuration, maxDuration);

			String outputPath = tempDir + "/" + outputName + ".gif";
			String paletteFile = tempDir + "/" + outputName + "_palette.png";

			// 1단계: 팔레트 생성
			String[] paletteCommand = {
				"ffmpeg", "-i", videoPath,
				"-t", String.valueOf(actualDuration),
				"-vf",
				"fps=20,scale=640:480:flags=lanczos:force_original_aspect_ratio=decrease,palettegen=stats_mode=diff",
				"-y", paletteFile
			};
			executeFFmpeg(paletteCommand);

			// 2단계: GIF 생성
			String[] gifCommand = {
				"ffmpeg", "-i", videoPath, "-i", paletteFile,
				"-t", String.valueOf(actualDuration),
				"-lavfi",
				"fps=20,scale=640:480:flags=lanczos:force_original_aspect_ratio=decrease[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=5:diff_mode=rectangle",
				"-loop", "0",
				"-y", outputPath
			};
			executeFFmpeg(gifCommand);

			// 팔레트 파일 삭제
			new File(paletteFile).delete();

			log.info("Created high-quality GIF: {}", outputPath);
			return outputPath;

		} catch (Exception e) {
			log.error("Failed to create high-quality GIF", e);
			throw new RuntimeException("High-quality GIF creation failed", e);
		}
	}

	/**
	 * 사이즈별 다양한 GIF 생성
	 */
	public Map<String, String> createMultipleSizeGifs(String videoPath, String baseOutputName, int maxDuration) {
		Map<String, String> gifPaths = new HashMap<>();

		try {
			double videoDuration = getVideoDuration(videoPath);
			int actualDuration = (int)Math.min(videoDuration, maxDuration);

			// 다양한 사이즈 설정
			Map<String, GifConfig> configs = Map.of(
				"small", new GifConfig(240, 180, 10,
					"fps=10,scale=240:180:force_original_aspect_ratio=decrease,pad=240:180:(ow-iw)/2:(oh-ih)/2"),
				"medium", new GifConfig(480, 360, 15,
					"fps=15,scale=480:360:force_original_aspect_ratio=decrease,pad=480:360:(ow-iw)/2:(oh-ih)/2"),
				"large", new GifConfig(640, 480, 20,
					"fps=20,scale=640:480:force_original_aspect_ratio=decrease,pad=640:480:(ow-iw)/2:(oh-ih)/2")
			);

			for (Map.Entry<String, GifConfig> entry : configs.entrySet()) {
				String sizeKey = entry.getKey();
				GifConfig config = entry.getValue();
				String outputPath = tempDir + "/" + baseOutputName + "_" + sizeKey + ".gif";

				String[] command = {
					"ffmpeg", "-i", videoPath,
					"-t", String.valueOf(actualDuration),
					"-vf", config.getFilter(),
					"-loop", "0",
					"-y", outputPath
				};

				executeFFmpeg(command);
				gifPaths.put(sizeKey, outputPath);

				log.info("Created {} GIF: {}", sizeKey, outputPath);
			}

		} catch (Exception e) {
			log.error("Failed to create multiple GIFs", e);
			throw new RuntimeException("Multiple GIF creation failed", e);
		}

		return gifPaths;
	}

	/**
	 * 비디오 길이 가져오기
	 */
	private double getVideoDuration(String videoPath) {
		try {
			String[] command = {
				"ffprobe", "-v", "quiet", "-show_entries", "format=duration",
				"-of", "csv=p=0", videoPath
			};

			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();

			String output = readProcessOutput(process);
			int exitCode = process.waitFor();

			if (exitCode == 0 && !output.trim().isEmpty()) {
				return Double.parseDouble(output.trim());
			} else {
				log.warn("Could not determine video duration, assuming 5 seconds");
				return 5.0;
			}

		} catch (Exception e) {
			log.error("Error getting video duration", e);
			return 5.0; // 기본값
		}
	}

	// FFmpeg 실행
	private void executeFFmpeg(String[] command) {
		try {
			log.info("Executing FFmpeg command: {}", String.join(" ", command));

			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process process = pb.start();

			// 프로세스 출력을 실시간으로 읽기
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
					log.debug("FFmpeg output: {}", line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				log.error("FFmpeg failed with exit code {}", exitCode);
				log.error("FFmpeg output: {}", output.toString());
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			log.info("FFmpeg command executed successfully");
		} catch (Exception e) {
			log.error("FFmpeg execution failed: {}", e.getMessage(), e);
			throw new OsaekException(THUMBNAIL_CANT_CREATE);
		}
	}

	private String readProcessOutput(Process process) throws IOException {
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

}
