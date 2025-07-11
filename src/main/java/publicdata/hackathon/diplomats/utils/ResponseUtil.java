package publicdata.hackathon.diplomats.utils;

import org.springframework.data.domain.Page;
import publicdata.hackathon.diplomats.domain.dto.response.PagedResponse;

import java.util.List;

public class ResponseUtil {
    
    /**
     * 현재 사용자가 작성자인지 확인
     */
    public static boolean isOwner(String currentUserId, String authorUserId) {
        if (currentUserId == null || authorUserId == null) {
            return false;
        }
        return currentUserId.equals(authorUserId);
    }
    
    /**
     * Page 객체를 PagedResponse로 변환
     */
    public static <T> PagedResponse<T> createPagedResponse(List<T> content, Page<?> page) {
        return PagedResponse.of(content, page);
    }
}
