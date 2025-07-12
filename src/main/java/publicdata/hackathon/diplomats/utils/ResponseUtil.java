package publicdata.hackathon.diplomats.utils;

import java.util.List;

import org.springframework.data.domain.Page;

import publicdata.hackathon.diplomats.domain.dto.response.PagedResponse;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.repository.NewsScrapRepository;
import publicdata.hackathon.diplomats.repository.PressReleaseRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

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
	 * 현재 사용자의 좋아요 상태 확인
	 */
	public static boolean isLiked(String currentUserId, String targetType, Long targetId,
		LikeRepository likeRepository, UserRepository userRepository) {
		if (currentUserId == null || targetId == null) {
			return false;
		}

		try {
			return userRepository.findByUserId(currentUserId)
				.map(user -> likeRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId))
				.orElse(false);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 현재 사용자의 스크랩 상태 확인
	 */
	public static boolean isScrapped(String currentUserId, Long newsId,
		NewsScrapRepository newsScrapRepository, UserRepository userRepository,
		PressReleaseRepository pressReleaseRepository) {
		if (currentUserId == null || newsId == null) {
			return false;
		}

		try {
			User user = userRepository.findByUserId(currentUserId).orElse(null);
			if (user == null) {
				return false;
			}

			// PressRelease 엔티티를 직접 조회해야 함
			return pressReleaseRepository.findById(newsId)
				.map(pressRelease -> newsScrapRepository.existsByUserAndPressRelease(user, pressRelease))
				.orElse(false);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Page 객체를 PagedResponse로 변환
	 */
	public static <T> PagedResponse<T> createPagedResponse(List<T> content, Page<?> page) {
		return PagedResponse.of(content, page);
	}
}
