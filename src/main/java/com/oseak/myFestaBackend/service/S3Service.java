package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.exception.OsaekException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket}")
	private String bucket;

	public String uploadFile(MultipartFile file, String folder) throws IOException {
		String fileName = generateFileName(file.getOriginalFilename(), folder);

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(fileName)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			return getFileUrl(fileName);
		} catch (Exception e) {
			log.error("S3 업로드 실패: {}", e.getMessage());
			throw new OsaekException(S3_UPLOAD_FAIL);
		}
	}

	public void deleteFile(String fileName) {
		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(fileName)
				.build();

			s3Client.deleteObject(deleteObjectRequest);
		} catch (Exception e) {
			log.error("S3 파일 삭제 실패: {}", e.getMessage());
			throw new OsaekException(S3_DELETE_FAIL);
		}
	}

	private String generateFileName(String originalFilename, String folder) {
		String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
		String uuid = UUID.randomUUID().toString();
		return folder + "/" + uuid + extension;
	}

	private String getFileUrl(String fileName) {
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket,
			s3Client.serviceClientConfiguration().region(), fileName);
	}
}
