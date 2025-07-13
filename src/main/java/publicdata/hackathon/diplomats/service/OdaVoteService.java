package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.OdaVoteRequest;
import publicdata.hackathon.diplomats.domain.dto.response.OdaVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.OdaVoteCandidateResponse;
import publicdata.hackathon.diplomats.domain.dto.response.OdaProjectResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampEarnedResponse;
import publicdata.hackathon.diplomats.domain.entity.OdaProject;
import publicdata.hackathon.diplomats.domain.entity.OdaVote;
import publicdata.hackathon.diplomats.domain.entity.OdaVoteCandidate;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserOdaVote;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;
import publicdata.hackathon.diplomats.repository.OdaProjectRepository;
import publicdata.hackathon.diplomats.repository.OdaVoteRepository;
import publicdata.hackathon.diplomats.repository.OdaVoteCandidateRepository;
import publicdata.hackathon.diplomats.repository.UserOdaVoteRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OdaVoteService {

	private final OdaVoteRepository odaVoteRepository;
	private final OdaVoteCandidateRepository odaVoteCandidateRepository;
	private final UserOdaVoteRepository userOdaVoteRepository;
	private final OdaProjectRepository odaProjectRepository;
	private final UserRepository userRepository;
	private final StampService stampService;

	/**
	 * 월별 ODA 투표를 생성합니다.
	 * 각 분야(환경, 교육, 보건, 여성, 기타)별로 1위 프로젝트를 후보로 선정합니다.
	 * 
	 * @return 투표 생성 결과 메시지
	 * @throws RuntimeException 이미 해당 월의 투표가 존재하거나 후보가 부족한 경우
	 */
	@Transactional
	public String createOdaVote() {
		YearMonth currentYearMonth = YearMonth.now();
		int year = currentYearMonth.getYear();
		int month = currentYearMonth.getMonthValue();

		if (odaVoteRepository.existsByYearAndMonth(year, month)) {
			throw new RuntimeException(year + "년 " + month + "월 ODA 투표가 이미 존재합니다.");
		}

		List<OdaProject> selectedProjects = selectTopProjectsFromEachCategory();

		if (selectedProjects.size() < 5) {
			throw new RuntimeException("투표에 필요한 최소 ODA 프로젝트가 부족합니다. (필요: 5개, 현재: " + selectedProjects.size() + "개)");
		}

		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = startDate.plusDays(30);

		OdaVote odaVote = OdaVote.builder()
			.year(year)
			.month(month)
			.title(year + "년 " + month + "월 의미있는 ODA 사업 투표")
			.description("이번 달 가장 의미있다고 생각하는 한국의 ODA(공적개발원조) 사업에 투표해주세요.")
			.startDate(startDate)
			.endDate(endDate)
			.build();

		odaVote = odaVoteRepository.save(odaVote);

		for (OdaProject project : selectedProjects) {
			OdaVoteCandidate candidate = OdaVoteCandidate.builder()
				.odaVote(odaVote)
				.odaProject(project)
				.voteCount(0)
				.build();
			odaVoteCandidateRepository.save(candidate);
		}

		log.info("{}년 {}월 ODA 투표가 생성되었습니다. 후보 수: {}", year, month, selectedProjects.size());
		return "ODA 투표가 성공적으로 생성되었습니다.";
	}

	private List<OdaProject> selectTopProjectsFromEachCategory() {
		List<String> categories = List.of("ENVIRONMENT", "EDUCATION", "HEALTH", "WOMEN", "OTHERS");
		
		return categories.stream()
			.map(category -> odaProjectRepository.findTop5ByCategoryOrderByMatchScoreDescPublishDateDesc(category))
			.filter(projects -> !projects.isEmpty())
			.map(projects -> projects.get(0))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public OdaVoteResponse getCurrentOdaVote() {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote().orElse(null);
		if (currentVote == null) return null;
		return convertToOdaVoteResponse(currentVote, null);
	}

	@Transactional(readOnly = true)
	public OdaVoteResponse getCurrentOdaVoteWithUserInfo(String userId) {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote().orElse(null);
		if (currentVote == null) return null;

		UserOdaVote userVote = userOdaVoteRepository.findByUserIdStringAndOdaVoteId(userId, currentVote.getId()).orElse(null);
		return convertToOdaVoteResponse(currentVote, userVote);
	}

	/**
	 * 사용자의 ODA 투표를 처리합니다.
	 * 사용자당 월 1회만 투표 가능하며, 투표 후 득표수가 즉시 반영됩니다.
	 * 
	 * @param userId 투표하는 사용자 ID
	 * @param voteRequest 투표 요청 (후보 ID 포함)
	 * @return 투표 완료 메시지
	 * @throws RuntimeException 중복 투표, 잘못된 후보, 투표 기간 외 등의 경우
	 */
	@Transactional
	public String vote(String userId, OdaVoteRequest voteRequest) {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote()
			.orElseThrow(() -> new RuntimeException("현재 진행 중인 ODA 투표가 없습니다."));

		User user = userRepository.findByUserId(userId)
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

		if (userOdaVoteRepository.existsByUserIdStringAndOdaVoteId(userId, currentVote.getId())) {
			throw new RuntimeException("이미 이번 달 ODA 투표에 참여하셨습니다.");
		}

		OdaVoteCandidate candidate = odaVoteCandidateRepository.findById(voteRequest.getCandidateId())
			.orElseThrow(() -> new RuntimeException("투표 후보를 찾을 수 없습니다."));

		if (!candidate.getOdaVote().getId().equals(currentVote.getId())) {
			throw new RuntimeException("잘못된 투표 후보입니다.");
		}

		UserOdaVote userOdaVote = UserOdaVote.builder()
			.user(user)
			.odaVote(currentVote)
			.odaVoteCandidate(candidate)
			.build();

		userOdaVoteRepository.save(userOdaVote);
		candidate.incrementVoteCount();
		odaVoteCandidateRepository.save(candidate);

		// 🎯 ODA 투표 참여 스탬프 지급
		try {
			StampEarnedResponse stampResponse = stampService.earnVoteStamp(user, currentVote.getId(), "ODA_VOTE");
			if (stampResponse.isSuccess()) {
				log.info("ODA 투표 참여 스탬프 지급 완료: userId={}, voteId={}, leveledUp={}", 
					userId, currentVote.getId(), stampResponse.isLeveledUp());
			}
		} catch (Exception e) {
			log.error("ODA 투표 참여 스탬프 지급 실패: userId={}, voteId={}", userId, currentVote.getId(), e);
		}

		log.info("ODA 투표 완료: userId={}, candidateId={}, voteId={}", userId, voteRequest.getCandidateId(), currentVote.getId());
		return "투표가 완료되었습니다.";
	}

	@Transactional(readOnly = true)
	public OdaVoteResponse getOdaVoteResult() {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote()
			.orElseThrow(() -> new RuntimeException("현재 진행 중인 ODA 투표가 없습니다."));
		return convertToOdaVoteResponse(currentVote, null);
	}

	private OdaVoteResponse convertToOdaVoteResponse(OdaVote vote, UserOdaVote userVote) {
		List<OdaVoteCandidate> candidates = odaVoteCandidateRepository.findByOdaVoteIdOrderByVoteCountDescMatchScoreDesc(vote.getId());
		
		long totalVoteCount = candidates.stream().mapToLong(candidate -> candidate.getVoteCount()).sum();

		List<OdaVoteCandidateResponse> candidateResponses = candidates.stream()
			.sorted(Comparator.comparing(OdaVoteCandidate::getVoteCount).reversed())
			.map(candidate -> {
				double percentage = totalVoteCount > 0 ? (double) candidate.getVoteCount() / totalVoteCount * 100 : 0.0;
				int rank = candidates.indexOf(candidate) + 1;

				return OdaVoteCandidateResponse.builder()
					.id(candidate.getId())
					.odaProject(convertToOdaProjectResponse(candidate.getOdaProject()))
					.voteCount(candidate.getVoteCount())
					.votePercentage(percentage)
					.rank(rank)
					.build();
			})
			.collect(Collectors.toList());

		boolean hasUserVoted = userVote != null;
		Long userVotedCandidateId = hasUserVoted ? userVote.getOdaVoteCandidate().getId() : null;

		return OdaVoteResponse.builder()
			.id(vote.getId())
			.year(vote.getYear())
			.month(vote.getMonth())
			.title(vote.getTitle())
			.description(vote.getDescription())
			.status(vote.getStatus())
			.startDate(vote.getStartDate())
			.endDate(vote.getEndDate())
			.createdAt(vote.getCreatedAt())
			.candidates(candidateResponses)
			.totalVoteCount(totalVoteCount)
			.hasUserVoted(hasUserVoted)
			.userVotedCandidateId(userVotedCandidateId)
			.build();
	}

	private OdaProjectResponse convertToOdaProjectResponse(OdaProject project) {
		String summary = project.getContent() != null && project.getContent().length() > 150 ? 
			project.getContent().substring(0, 150) + "..." : project.getContent();

		return OdaProjectResponse.builder()
			.id(project.getId())
			.title(project.getTitle())
			.content(project.getContent())
			.url(project.getUrl())
			.category(project.getCategory())
			.countryName(project.getCountryName())
			.projectStartDate(project.getProjectStartDate())
			.projectEndDate(project.getProjectEndDate())
			.budget(project.getBudget())
			.publishDate(project.getPublishDate())
			.matchScore(project.getMatchScore())
			.summary(summary)
			.build();
	}

	@Transactional(readOnly = true)
	public OdaVoteResponse getMyOdaVote(String userId) {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote().orElse(null);
		if (currentVote == null) return null;

		UserOdaVote userVote = userOdaVoteRepository.findByUserIdStringAndOdaVoteId(userId, currentVote.getId()).orElse(null);
		return convertToOdaVoteResponse(currentVote, userVote);
	}

	@Transactional
	public String endCurrentOdaVote() {
		OdaVote currentVote = odaVoteRepository.findCurrentActiveVote()
			.orElseThrow(() -> new RuntimeException("현재 진행 중인 ODA 투표가 없습니다."));

		currentVote.setStatus(VoteStatus.CLOSED);
		odaVoteRepository.save(currentVote);

		log.info("{}년 {}월 ODA 투표가 종료되었습니다.", currentVote.getYear(), currentVote.getMonth());
		return "ODA 투표가 종료되었습니다.";
	}

	/**
	 * 특정 월의 ODA 투표 결과를 조회합니다.
	 * 
	 * @param year 조회할 년도
	 * @param month 조회할 월
	 * @return ODA 투표 결과 응답
	 * @throws RuntimeException 해당 월의 투표가 없는 경우
	 */
	@Transactional(readOnly = true)
	public OdaVoteResponse getOdaVoteResultByMonth(Integer year, Integer month) {
		log.info("특정 월 ODA 투표 결과 조회: year={}, month={}", year, month);
		
		OdaVote vote = odaVoteRepository.findByYearAndMonth(year, month)
			.orElseThrow(() -> new RuntimeException(year + "년 " + month + "월 ODA 투표를 찾을 수 없습니다."));
		
		return convertToOdaVoteResponse(vote, null);
	}
}
