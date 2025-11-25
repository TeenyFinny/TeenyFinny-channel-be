package dev.syntax.domain.goal.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreGoalAccountRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

    private final CoreAccountClient coreAccountClient;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    private final GoalRepository goalRepository;

    @Transactional
    public void createGoalAccount(Long userId, String goalName) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 2. 검증 로직
        if (goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_ONGOING);
        }

        // 3. 코어 서버 목표계좌 생성 요청
        CoreGoalAccountRes coreRes = coreAccountClient.createGoalAccount(userId, goalName);
        if (coreRes == null || coreRes.getAccountNumber() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }

        // 4. 채널 DB에 계좌 정보 저장

        Account account = Account.builder()
                .user(user)
                .type(AccountType.GOAL)
                .accountNo(coreRes.getAccountNumber())
                .build();

        accountRepository.save(account);

        // 5. 자동 이체 생성 로직 TODO
        // - 목표 계좌 개설 이후, monthlyAmount/payDay 기준으로
        //   자동이체 생성 API 개발되면 연동할 예정
        // - core 서버 자동이체 엔드포인트 완성되면 여기서 호출

        // 6. 계좌 생성 완료 후 추가 응답 데이터가 필요하다면 여기에 로직 추가
        // ex) Goal 생성 결과 / 자동이체 정보 / 계좌 메타데이터 등
    }
}
