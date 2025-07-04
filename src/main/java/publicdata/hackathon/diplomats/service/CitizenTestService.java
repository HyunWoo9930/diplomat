package publicdata.hackathon.diplomats.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.AnswerDto;
import publicdata.hackathon.diplomats.domain.dto.request.SubmitAnswersRequest;
import publicdata.hackathon.diplomats.domain.dto.response.CitizenTestQuestionsResponse;
import publicdata.hackathon.diplomats.domain.dto.response.CitizenTestResultResponse;
import publicdata.hackathon.diplomats.domain.dto.response.QuestionOptionResponse;
import publicdata.hackathon.diplomats.domain.dto.response.QuestionResponse;
import publicdata.hackathon.diplomats.domain.dto.response.RecommendedDiplomacyProgram;
import publicdata.hackathon.diplomats.domain.dto.response.RecommendedPressRelease;
import publicdata.hackathon.diplomats.domain.entity.CitizenType;
import publicdata.hackathon.diplomats.domain.entity.Question;
import publicdata.hackathon.diplomats.domain.entity.QuestionOption;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.CitizenTypeRepository;
import publicdata.hackathon.diplomats.repository.QuestionOptionRepository;
import publicdata.hackathon.diplomats.repository.QuestionRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitizenTestService {

	private final QuestionRepository questionRepository;
	private final QuestionOptionRepository questionOptionRepository;
	private final CitizenTypeRepository citizenTypeRepository;
	private final UserRepository userRepository;
	private final PressReleaseService pressReleaseService;
	private final PublicDiplomacyService publicDiplomacyService;

	public CitizenTestQuestionsResponse getAllQuestions() {
		List<Question> questions = questionRepository.findAllWithOptionsOrderByQuestionOrder();

		List<QuestionResponse> questionResponses = questions.stream()
			.map(this::convertToQuestionResponse)
			.collect(Collectors.toList());

		return CitizenTestQuestionsResponse.builder()
			.questions(questionResponses)
			.totalQuestions(questionResponses.size())
			.message("질문 조회 성공")
			.build();
	}

	private QuestionResponse convertToQuestionResponse(Question question) {
		List<QuestionOptionResponse> optionResponses = question.getOptions().stream()
			.map(option -> QuestionOptionResponse.builder()
				.id(option.getId())
				.optionText(option.getOptionText())
				.optionOrder(option.getOptionOrder())
				.build())
			.collect(Collectors.toList());

		return QuestionResponse.builder()
			.id(question.getId())
			.content(question.getContent())
			.questionOrder(question.getQuestionOrder())
			.options(optionResponses)
			.build();
	}

	@Transactional
	public CitizenTestResultResponse submitAnswers(String username, SubmitAnswersRequest request) {
		// 1. 사용자 조회
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		// 2. 선택한 옵션들 조회
		List<Long> optionIds = request.getAnswers().stream()
			.map(AnswerDto::getOptionId)
			.collect(Collectors.toList());

		List<QuestionOption> selectedOptions = questionOptionRepository.findByIdIn(optionIds);
		if (selectedOptions.size() != request.getAnswers().size()) {
			throw new RuntimeException("일부 선택지를 찾을 수 없습니다.");
		}
		Map<String, Integer> typeScores = calculateTypeScores(selectedOptions);
		String resultTypeName = determineResultType(typeScores);
		user.setCitizenType(resultTypeName);
		userRepository.save(user);

		// 6. 유형 정보 조회 및 추천 보도자료 가져오기
		CitizenType citizenType = citizenTypeRepository.findByTypeName(resultTypeName)
			.orElseThrow(() -> new EntityNotFoundException("시민력 유형을 찾을 수 없습니다."));

		List<RecommendedPressRelease> recommendedNews = pressReleaseService.getRecommendedPressReleases(resultTypeName);
		List<RecommendedDiplomacyProgram> recommendedPrograms = publicDiplomacyService.getRecommendedPrograms(
			resultTypeName);

		return CitizenTestResultResponse.builder()
			.resultType(citizenType.getTypeName())
			.displayName(citizenType.getDisplayName())
			.description(citizenType.getDescription())
			.recommendedNews(recommendedNews)
			.recommendedPrograms(recommendedPrograms)  // 추가
			.message("시민력 테스트가 완료되었습니다!")
			.build();
	}

	private Map<String, Integer> calculateTypeScores(List<QuestionOption> selectedOptions) {
		Map<String, Integer> typeScores = new HashMap<>();

		for (QuestionOption option : selectedOptions) {
			String scoreType = option.getScoreType();
			Integer scoreValue = option.getScoreValue();

			typeScores.put(scoreType, typeScores.getOrDefault(scoreType, 0) + scoreValue);
		}

		return typeScores;
	}

	private String determineResultType(Map<String, Integer> typeScores) {
		return typeScores.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse("CULTURAL_DIPLOMACY"); // 기본값
	}

	// CitizenTestService.java에 추가
	public CitizenTestResultResponse getMyTestResult(String username) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		if (user.getCitizenType() == null || user.getCitizenType().isEmpty()) {
			// 테스트를 하지 않은 경우
			return CitizenTestResultResponse.builder()
				.resultType(null)
				.displayName(null)
				.description(null)
				.recommendedNews(Collections.emptyList())
				.message("아직 시민력 테스트를 완료하지 않았습니다. 테스트를 통해 나만의 시민력 유형을 확인해보세요!")
				.build();
		}

		CitizenType citizenType = citizenTypeRepository.findByTypeName(user.getCitizenType())
			.orElseThrow(() -> new EntityNotFoundException("시민력 유형을 찾을 수 없습니다."));

		List<RecommendedPressRelease> recommendedNews = pressReleaseService.getRecommendedPressReleases(
			user.getCitizenType());
		List<RecommendedDiplomacyProgram> recommendedPrograms = publicDiplomacyService.getRecommendedPrograms(
			user.getCitizenType());

		return CitizenTestResultResponse.builder()
			.resultType(citizenType.getTypeName())
			.displayName(citizenType.getDisplayName())
			.description(citizenType.getDescription())
			.recommendedNews(recommendedNews)
			.recommendedPrograms(recommendedPrograms)  // 추가
			.message("시민력 테스트가 완료되었습니다!")
			.build();
	}
}