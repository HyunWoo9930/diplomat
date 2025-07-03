package publicdata.hackathon.diplomats.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class ImageUtil {

	public String encodeImageToBase64(String imagePath) {
		try {
			Path path = Paths.get(imagePath);
			byte[] imageBytes = Files.readAllBytes(path);
			return Base64.getEncoder().encodeToString(imageBytes);
		} catch (IOException e) {
			throw new RuntimeException("이미지 인코딩에 실패했습니다.", e);
		}
	}

	public String getImageMimeType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		switch (extension) {
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			default:
				return "image/jpeg";
		}
	}
}