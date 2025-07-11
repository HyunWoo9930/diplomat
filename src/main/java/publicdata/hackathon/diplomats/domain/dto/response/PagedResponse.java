package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private PaginationMeta pagination;
    
    @Data
    @Builder
    public static class PaginationMeta {
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private long totalCount;
        private boolean hasMore;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;
    }
    
    public static <T> PagedResponse<T> of(List<T> content, org.springframework.data.domain.Page<?> page) {
        return PagedResponse.<T>builder()
                .content(content)
                .pagination(PaginationMeta.builder()
                        .currentPage(page.getNumber() + 1) // 0-based to 1-based
                        .totalPages(page.getTotalPages())
                        .pageSize(page.getSize())
                        .totalCount(page.getTotalElements())
                        .hasMore(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .isFirst(page.isFirst())
                        .isLast(page.isLast())
                        .build())
                .build();
    }
}
