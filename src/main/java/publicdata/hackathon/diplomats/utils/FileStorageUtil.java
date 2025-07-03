package publicdata.hackathon.diplomats.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Component
public class FileStorageUtil {

	private final String uploadDir = "uploads/freeboard/";

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(Paths.get(uploadDir));
		} catch (IOException e) {
			throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다.", e);
		}
	}

	public String saveFile(MultipartFile file) {
		try {
			// 파일명 생성 (UUID + 원본 파일명)
			String originalFileName = file.getOriginalFilename();
			String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			String savedFileName = UUID.randomUUID().toString() + extension;

			// 파일 저장
			Path targetLocation = Paths.get(uploadDir + savedFileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return savedFileName; // 저장된 파일명 반환
		} catch (IOException e) {
			throw new RuntimeException("파일 저장에 실패했습니다.", e);
		}
	}

	public void deleteFile(String savedFileName) {
		try {
			Path filePath = Paths.get(uploadDir + savedFileName);
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new RuntimeException("파일 삭제에 실패했습니다.", e);
		}
	}
}