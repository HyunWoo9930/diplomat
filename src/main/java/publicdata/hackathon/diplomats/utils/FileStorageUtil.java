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

    private final String baseUploadDir = "uploads/";
    private final String freeBoardDir = "uploads/freeboard/";
    private final String discussBoardDir = "uploads/discussboard/";
    private final String diaryDir = "uploads/diary/";

    @PostConstruct
    public void init() {
       try {
          // 모든 업로드 디렉토리 생성
          Files.createDirectories(Paths.get(freeBoardDir));
          Files.createDirectories(Paths.get(discussBoardDir));
          Files.createDirectories(Paths.get(diaryDir));
       } catch (IOException e) {
          throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다.", e);
       }
    }

    /**
     * 파일을 지정된 디렉토리에 저장
     * @param file 저장할 파일
     * @param uploadDir 저장할 디렉토리 (예: "uploads/freeboard/")
     * @return 저장된 파일명
     */
    public String saveFile(MultipartFile file, String uploadDir) {
       try {
          // 디렉토리가 존재하지 않으면 생성
          Files.createDirectories(Paths.get(uploadDir));
          
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

    /**
     * FreeBoard용 파일 저장 (기존 호환성 유지)
     * @param file 저장할 파일
     * @return 저장된 파일명
     */
    public String saveFile(MultipartFile file) {
       return saveFile(file, freeBoardDir);
    }

    /**
     * FreeBoard용 파일 저장
     * @param file 저장할 파일
     * @return 저장된 파일명
     */
    public String saveFreeBoardFile(MultipartFile file) {
       return saveFile(file, freeBoardDir);
    }

    /**
     * DiscussBoard용 파일 저장
     * @param file 저장할 파일
     * @return 저장된 파일명
     */
    public String saveDiscussBoardFile(MultipartFile file) {
       return saveFile(file, discussBoardDir);
    }

    /**
     * Diary용 파일 저장
     * @param file 저장할 파일
     * @return 저장된 파일명
     */
    public String saveDiaryFile(MultipartFile file) {
       return saveFile(file, diaryDir);
    }

    /**
     * 파일 삭제
     * @param savedFileName 삭제할 파일명
     * @param uploadDir 파일이 저장된 디렉토리
     */
    public void deleteFile(String savedFileName, String uploadDir) {
       try {
          Path filePath = Paths.get(uploadDir + savedFileName);
          Files.deleteIfExists(filePath);
       } catch (IOException e) {
          throw new RuntimeException("파일 삭제에 실패했습니다.", e);
       }
    }

    /**
     * FreeBoard 파일 삭제 (기존 호환성 유지)
     * @param savedFileName 삭제할 파일명
     */
    public void deleteFile(String savedFileName) {
       deleteFile(savedFileName, freeBoardDir);
    }

    /**
     * FreeBoard 파일 삭제
     * @param savedFileName 삭제할 파일명
     */
    public void deleteFreeBoardFile(String savedFileName) {
       deleteFile(savedFileName, freeBoardDir);
    }

    /**
     * DiscussBoard 파일 삭제
     * @param savedFileName 삭제할 파일명
     */
    public void deleteDiscussBoardFile(String savedFileName) {
       deleteFile(savedFileName, discussBoardDir);
    }

    /**
     * Diary 파일 삭제
     * @param savedFileName 삭제할 파일명
     */
    public void deleteDiaryFile(String savedFileName) {
       deleteFile(savedFileName, diaryDir);
    }
}
