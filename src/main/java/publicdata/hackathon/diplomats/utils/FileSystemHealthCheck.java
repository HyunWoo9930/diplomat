package publicdata.hackathon.diplomats.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileSystemHealthCheck {

    private final String[] uploadDirectories = {
        "uploads/freeboard/",
        "uploads/discussboard/", 
        "uploads/diary/"
    };

    @EventListener(ApplicationReadyEvent.class)
    public void checkFileSystemHealth() {
        log.info("파일 시스템 상태 확인 시작");
        
        boolean allHealthy = true;
        
        for (String directory : uploadDirectories) {
            if (checkDirectory(directory)) {
                log.info("✅ 디렉토리 정상: {}", directory);
            } else {
                log.error("❌ 디렉토리 문제: {}", directory);
                allHealthy = false;
            }
        }
        
        if (allHealthy) {
            log.info("🎉 모든 업로드 디렉토리가 정상 상태입니다");
        } else {
            log.warn("⚠️ 일부 업로드 디렉토리에 문제가 있습니다");
        }
    }
    
    private boolean checkDirectory(String directory) {
        try {
            Path path = Paths.get(directory);
            
            // 디렉토리 존재 여부
            if (!Files.exists(path)) {
                log.warn("디렉토리가 존재하지 않음: {}", directory);
                return false;
            }
            
            // 디렉토리인지 확인
            if (!Files.isDirectory(path)) {
                log.warn("파일이 디렉토리가 아님: {}", directory);
                return false;
            }
            
            // 읽기 권한 확인
            if (!Files.isReadable(path)) {
                log.warn("디렉토리 읽기 권한 없음: {}", directory);
                return false;
            }
            
            // 쓰기 권한 확인
            if (!Files.isWritable(path)) {
                log.warn("디렉토리 쓰기 권한 없음: {}", directory);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("디렉토리 상태 확인 실패: {}", directory, e);
            return false;
        }
    }
}
