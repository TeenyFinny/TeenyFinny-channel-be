package dev.syntax.domain.goal.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreGoalAccountRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.dto.GoalAccountCreateRes;
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


/**
 * GoalAccountServiceImpl
 *
 * <p>사용자 목표 계좌 생성 관련 비즈니스 로직을 수행하는 서비스입니다.</p>
 */
@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

    private final CoreAccountClient coreAccountClient;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final GoalRepository goalRepository;

    /**
     * 목표 계좌를 생성합니다.
     *
     * <p>주요 처리 과정:</p>
     * <ol>
     *     <li>사용자 조회: userId로 User 엔티티 조회, 없으면 실패 반환</li>
     *     <li>진행 중인 목표 존재 여부 검증: 이미 진행 중이면 실패 반환</li>
     *     <li>코어 서버에 목표 계좌 생성 요청</li>
     *     <li>계좌 정보 DB 저장</li>
     *     <li>처리 도중 예외가 발생하면 false 반환, 정상 처리 시 true 반환</li>
     * </ol>
     *
     * @param userId   계좌를 생성할 사용자의 ID
     * @param goalName 목표 이름
     * @return GoalAccountCreateRes 계좌 생성 성공 여부
     */
    @Transactional
    public GoalAccountCreateRes createGoalAccount(Long userId, String goalName) {
        try {
            // 1. 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

            // 2. 진행 중인 목표 검증
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

            // 5. 성공 반환
            return GoalAccountCreateRes.builder()
                    .success(true)
                    .build();

        } catch (BusinessException ex) {
            // 실패 시 false 반환
            return GoalAccountCreateRes.builder()
                    .success(false)
                    .build();
        }
    }
}
