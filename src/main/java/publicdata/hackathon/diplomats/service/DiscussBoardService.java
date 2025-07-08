package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoardImage;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardImageRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;

@Service
@RequiredArgsConstructor
public class DiscussBoardService {

	private final DiscussBoardRepository discussBoardRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final DiscussBoardImageRepository discussBoardImageRepository;
	private final UserRepository userRepository;
	private final FileStorageUtil fileStorageUtil;

	public void createDiscussBoard(String username, String title, String content, DiscussType discussType,
		List<MultipartFile> images) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("User not found"));
		DiscussBoard discussBoard = DiscussBoard.builder()
			.discussType(discussType)
			.content(content)
			.title(title)
			.user(user)
			.build();

		discussBoardRepository.save(discussBoard);

		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
				if (!image.isEmpty()) {
					String savedFileName = fileStorageUtil.saveDiscussBoardFile(image);

					DiscussBoardImage discussBoardImage = DiscussBoardImage.builder()
						.discussBoard(discussBoard)
						.imagePath("/uploads/discussboard/" + savedFileName)
						.originalFileName(image.getOriginalFilename())
						.savedFileName(savedFileName)
						.fileSize(image.getSize())
						.contentType(image.getContentType())
						.imageOrder(i + 1) // 1부터 시작하는 순서
						.uploadedAt(LocalDateTime.now())
						.build();

					discussBoardImageRepository.save(discussBoardImage);
				}
			}
		}
	}
}
