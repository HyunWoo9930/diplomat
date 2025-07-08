package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.VoteRequest;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.VoteBannerResponse;
import publicdata.hackathon.diplomats.domain.dto.response.VoteCandidateResponse;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.MonthlyVote;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserVote;
import publicdata.hackathon.diplomats.domain.entity.VoteCandidate;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.MonthlyVoteRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.UserVoteRepository;
import publicdata.hackathon.diplomats.repository.VoteCandidateRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyVoteService {

	private final MonthlyVoteRepository monthlyVoteRepository;
	private final VoteCandidateRepository voteCandidateRepository;
	private final UserVoteRepository userVoteRepository;
	private final DiaryRepository diaryRepository;
	private final UserRepository userRepository;

	/**
	 * 월별 투표 생성 (이번 달 인기 일지 상위 10개로)
	 */
	public String createMonthlyVote() {
		YearMonth currentMonth = YearMonth.now();
		int year = currentMonth.getYear();
		int month = currentMonth.getMonthValue();

		// 이미 이번 달 투표가 있는지 확인
		if (monthlyVoteRepository.existsByYearAndMonth(year, month)) {
			throw new RuntimeException("이번 달 투표가 이미 존재합니다.");
		}

		// 이번 달 인기 일지 상위 10개 조회
		LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
		LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
		
		Pageable top10 = PageRequest.of(0, 10);
		List<Diary> topDiaries = diaryRepository.findTopDiariesByMonth(startOfMonth, endOfMonth, top10);

		if (topDiaries.size() < 3) {
			throw new RuntimeException("투표를 생성하기에 충분한 일지가 없습니다. (최소 3개 필요)");
		}

		// 투표 생성
		MonthlyVote monthlyVote = MonthlyVote.builder()
			.year(year)
			.month(month)
			.title(year + "년 " + month + "월 이달의 외교 실천왕")
			.description("이번 달 가장 인상적인 외교 실천 사례를 선정해주세요!")
			.startDate(LocalDateTime.now())
			.endDate(currentMonth.atEndOfMonth().atTime(23, 59, 59))
			.build();

		monthlyVoteRepository.save(monthlyVote);

		// 후보 등록
		for (int i = 0; i < topDiaries.size(); i++) {
			VoteCandidate candidate = VoteCandidate.builder()
				.monthlyVote(monthlyVote)
				.diary(topDiaries.get(i))
				.ranking(i + 1)
				.build();
			voteCandidateRepository.save(candidate);
		}

		return "투표가 성공적으로 생성되었습니다. 후보 수: " + topDiaries.size();
	}

	/**
	 * 현재 활성 투표 조회
	 */
	@Transactional(readOnly = true)
	public MonthlyVoteResponse getCurrentVote() {
		Optional<MonthlyVote> currentVote = monthlyVoteRepository.findCurrentActiveVote();
		
		if (currentVote.isEmpty()) {
			return null; // 현재 활성 투표 없음
		}

		MonthlyVote vote = currentVote.get();
		List<VoteCandidate> candidates = voteCandidateRepository.findByMonthlyVoteOrderByRanking(vote);
		Long totalVotes = userVoteRepository.countByMonthlyVote(vote);

		List<VoteCandidateResponse> candidateResponses = candidates.stream()
			.map(this::mapToVoteCandidateResponse)
			.toList();

		return MonthlyVoteResponse.builder()
			.id(vote.getId())
			.year(vote.getYear())
			.month(vote.getMonth())
			.title(vote.getTitle())
			.description(vote.getDescription())
			.status(vote.getStatus())
			.startDate(vote.getStartDate())
			.endDate(vote.getEndDate())
			.totalVotes(totalVotes)
			.candidates(candidateResponses)
			.build();
	}

	/**
	 * 투표 참여
	 */
	public String vote(String username, VoteRequest voteRequest) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		MonthlyVote currentVote = monthlyVoteRepository.findCurrentActiveVote()
			.orElseThrow(() -> new RuntimeException("현재 진행중인 투표가 없습니다."));

		// 이미 투표했는지 확인
		if (userVoteRepository.existsByUserAndMonthlyVote(user, currentVote)) {
			throw new RuntimeException("이미 투표에 참여하셨습니다.");
		}

		// 투표 기간 확인
		if (LocalDateTime.now().isAfter(currentVote.getEndDate())) {
			throw new RuntimeException("투표 기간이 종료되었습니다.");
		}

		VoteCandidate candidate = voteCandidateRepository.findById(voteRequest.getCandidateId())
			.orElseThrow(() -> new EntityNotFoundException("투표 후보를 찾을 수 없습니다."));

		// 후보가 현재 투표에 속하는지 확인
		if (!candidate.getMonthlyVote().getId().equals(currentVote.getId())) {
			throw new RuntimeException("유효하지 않은 투표 후보입니다.");
		}

		// 투표 생성
		UserVote userVote = UserVote.builder()
			.user(user)
			.monthlyVote(currentVote)
			.voteCandidate(candidate)
			.build();

		userVoteRepository.save(userVote);

		// 후보 득표수 증가
		candidate.addVote();
		voteCandidateRepository.save(candidate);

		return "투표가 완료되었습니다.";
	}

	/**
	 * 내 투표 내역 조회
	 */
	@Transactional(readOnly = true)
	public UserVoteResponse getMyVote(String username) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		MonthlyVote currentVote = monthlyVoteRepository.findCurrentActiveVote().orElse(null);
		
		if (currentVote == null) {
			return UserVoteResponse.builder()
				.hasVoted(false)
				.build();
		}

		Optional<UserVote> userVote = userVoteRepository.findByUserAndMonthlyVote(user, currentVote);

		if (userVote.isEmpty()) {
			return UserVoteResponse.builder()
				.hasVoted(false)
				.build();
		}

		UserVote vote = userVote.get();
		return UserVoteResponse.builder()
			.hasVoted(true)
			.votedCandidateId(vote.getVoteCandidate().getId())
			.votedDiaryTitle(vote.getVoteCandidate().getDiary().getTitle())
			.votedAt(vote.getVotedAt())
			.build();
	}

	/**
	 * 투표 결과 조회
	 */
	@Transactional(readOnly = true)
	public MonthlyVoteResponse getVoteResult() {
		MonthlyVote currentVote = monthlyVoteRepository.findCurrentActiveVote()
			.orElseThrow(() -> new RuntimeException("현재 진행중인 투표가 없습니다."));

		List<VoteCandidate> candidates = voteCandidateRepository.findByMonthlyVoteOrderByVoteCountDesc(currentVote);
		Long totalVotes = userVoteRepository.countByMonthlyVote(currentVote);

		List<VoteCandidateResponse> candidateResponses = candidates.stream()
			.map(this::mapToVoteCandidateResponse)
			.toList();

		return MonthlyVoteResponse.builder()
			.id(currentVote.getId())
			.year(currentVote.getYear())
			.month(currentVote.getMonth())
			.title(currentVote.getTitle())
			.description(currentVote.getDescription())
			.status(currentVote.getStatus())
			.startDate(currentVote.getStartDate())
			.endDate(currentVote.getEndDate())
			.totalVotes(totalVotes)
			.candidates(candidateResponses)
			.build();
	}

	/**
	 * 메인페이지 투표 배너 정보
	 */
	@Transactional(readOnly = true)
	public VoteBannerResponse getVoteBanner() {
		Optional<MonthlyVote> currentVote = monthlyVoteRepository.findCurrentActiveVote();
		
		if (currentVote.isEmpty()) {
			return VoteBannerResponse.builder()
				.hasActiveVote(false)
				.build();
		}

		MonthlyVote vote = currentVote.get();
		Long totalVotes = userVoteRepository.countByMonthlyVote(vote);
		List<VoteCandidate> candidates = voteCandidateRepository.findByMonthlyVoteOrderByVoteCountDesc(vote);
		
		String topCandidateTitle = candidates.isEmpty() ? null : candidates.get(0).getDiary().getTitle();

		return VoteBannerResponse.builder()
			.hasActiveVote(true)
			.title(vote.getTitle())
			.status(vote.getStatus())
			.endDate(vote.getEndDate())
			.totalVotes(totalVotes)
			.totalCandidates(candidates.size())
			.topCandidateTitle(topCandidateTitle)
			.build();
	}

	private VoteCandidateResponse mapToVoteCandidateResponse(VoteCandidate candidate) {
		Diary diary = candidate.getDiary();
		return VoteCandidateResponse.builder()
			.candidateId(candidate.getId())
			.diaryId(diary.getId())
			.diaryTitle(diary.getTitle())
			.diaryDescription(diary.getDescription())
			.diaryAction(diary.getAction())
			.authorName(diary.getWriter().getName())
			.diaryCreatedAt(diary.getCreatedAt())
			.diaryLikes(diary.getLikes())
			.diaryViewCount(diary.getViewCount())
			.voteCount(candidate.getVoteCount())
			.ranking(candidate.getRanking())
			.build();
	}
}
