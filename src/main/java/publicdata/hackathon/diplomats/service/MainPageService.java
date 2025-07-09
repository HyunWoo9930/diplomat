package publicdata.hackathon.diplomats.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.CommunityPostResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MainPageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.NewsResponse;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.PressRelease;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.PressReleaseRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MainPageService {

    private final DiaryRepository diaryRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final PressReleaseRepository pressReleaseRepository;
    private final DiscussBoardRepository discussBoardRepository;

    /**
     * 메인페이지 전체 데이터 조회
     */
    public MainPageResponse getMainPageData() {
        log.info("메인페이지 데이터 조회 시작");

        // 각각 3개씩 조회
        List<DiaryResponse> recentDiaries = getRecentDiaries();
        List<NewsResponse> recentNews = getRecentNews();
        List<CommunityPostResponse> popularPosts = getPopularCommunityPosts();

        log.info("메인페이지 데이터 조회 완료 - 일지: {}개, 뉴스: {}개, 커뮤니티: {}개",
                recentDiaries.size(), recentNews.size(), popularPosts.size());

        return MainPageResponse.builder()
            .recentDiaries(recentDiaries)
            .recentNews(recentNews)
            .popularCommunityPosts(popularPosts)
            .build();
    }

    /**
     * 최신 외교일지 3개 조회
     */
    private List<DiaryResponse> getRecentDiaries() {
        Pageable top3 = PageRequest.of(0, 3);
        List<Diary> diaries = diaryRepository.findAllByOrderByCreatedAtDesc(top3).getContent();

        return diaries.stream()
            .map(diary -> DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .description(createSummary(diary.getDescription()))
                .action(diary.getAction())
                .likes(diary.getLikes())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .userId(diary.getWriter().getUserId())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 최신 외교뉴스 3개 조회
     */
    private List<NewsResponse> getRecentNews() {
        Pageable top3 = PageRequest.of(0, 3);
        List<PressRelease> newsList = pressReleaseRepository.findAllByOrderByPublishDateDesc(top3);

        return newsList.stream()
            .map(news -> NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .summary(createSummary(news.getContent()))
                // .source(news.getSource())
                .url(news.getUrl())
                // .imageUrl(news.getImageUrl())
                // .category(news.getCategory())
                .publishDate(news.getPublishDate().atStartOfDay())
                .createdAt(news.getCreatedAt())
                // .viewCount(news.getViewCount())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 커뮤니티 인기글 3개 조회 (자유게시판 + 토론게시판)
     */
    private List<CommunityPostResponse> getPopularCommunityPosts() {
        Pageable top3 = PageRequest.of(0, 3);

        // 자유게시판 인기글 조회
        List<FreeBoard> popularFreeBoards = freeBoardRepository.findAllByOrderByLikesDescCreatedAtDesc(top3);

        // 토론게시판 인기글 조회
        List<DiscussBoard> popularDiscussBoards = discussBoardRepository.findAllByOrderByLikesDescCreatedAtDesc(top3);

        // 자유게시판 변환
        List<CommunityPostResponse> freeBoardResponses = popularFreeBoards.stream()
            .map(board -> CommunityPostResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .summary(createSummary(board.getContent()))
                .authorName(board.getUser().getName())
                .authorId(board.getUser().getUserId())
                .likes(board.getLikes())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .boardType("FREE")
                .boardTypeName("자유게시판")
                .build())
            .collect(Collectors.toList());

        // 토론게시판 변환
        List<CommunityPostResponse> discussBoardResponses = popularDiscussBoards.stream()
            .map(board -> CommunityPostResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .summary(createSummary(board.getContent()))
                .authorName(board.getUser().getName())
                .authorId(board.getUser().getUserId())
                .likes(board.getLikes())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .boardType("DISCUSS")
                .boardTypeName("토론게시판")
                .build())
            .collect(Collectors.toList());

        // 두 게시판 결합하고 좋아요 수 기준으로 정렬 후 상위 3개 반환
        return freeBoardResponses.stream()
            .collect(Collectors.toList())
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * 텍스트 요약 생성 (150자 제한)
     */
    private String createSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // HTML 태그 제거
        String cleaned = text.replaceAll("<[^>]*>", "")
            .replaceAll("\\s+", " ")
            .trim();

        if (cleaned.length() <= 150) {
            return cleaned;
        }

        return cleaned.substring(0, 150) + "...";
    }

    /**
     * 내용으로부터 카테고리 결정
     */
    private String determineCategoryFromContent(String content) {
        if (content == null) return "일반";

        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("기후") || lowerContent.contains("환경")) {
            return "기후환경";
        } else if (lowerContent.contains("문화") || lowerContent.contains("예술")) {
            return "문화외교";
        } else if (lowerContent.contains("oda") || lowerContent.contains("개발협력")) {
            return "개발협력";
        } else if (lowerContent.contains("경제") || lowerContent.contains("통상")) {
            return "경제통상";
        }

        return "일반";
    }
}
