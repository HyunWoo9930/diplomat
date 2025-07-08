package publicdata.hackathon.diplomats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.service.MonthlyVoteService;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

	private final MonthlyVoteService monthlyVoteService;

	/**
	 * 매월 1일 오전 9시에 투표 생성
	 */
	@Scheduled(cron = "0 0 9 1 * *")
	public void createMonthlyVote() {
		try {
			log.info("월별 투표 자동 생성 시작");
			String result = monthlyVoteService.createMonthlyVote();
			log.info("월별 투표 자동 생성 완료: {}", result);
		} catch (Exception e) {
			log.error("월별 투표 자동 생성 실패: {}", e.getMessage());
		}
	}

	/**
	 * 매일 오전 10시에 투표 상태 체크 (수동으로 마감 처리가 필요한 경우를 위해)
	 */
	@Scheduled(cron = "0 0 10 * * *")
	public void checkVoteStatus() {
		log.info("투표 상태 체크 실행");
		// 필요시 투표 마감 로직 추가
	}
}
