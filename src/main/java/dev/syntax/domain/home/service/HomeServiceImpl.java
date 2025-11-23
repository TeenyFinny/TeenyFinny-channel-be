package dev.syntax.domain.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.BalanceProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

	private final UserRepository userRepository;
	private final BalanceProvider balanceProvider;

	/**
	 * 사용자 역할(부모/자녀)에 따라 적절한 홈 화면 데이터를 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 홈 화면 데이터 응답
	 */
	public HomeRes getHomeData(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		if (user.getRole() == Role.PARENT) {
			return getParentHomeData(user);
		} else {
			return getChildHomeData(user);
		}
	}

	/**
	 * 부모 사용자의 홈 화면 데이터를 생성합니다.
	 * 자녀 목록과 각 자녀의 잔액 정보를 포함합니다.
	 *
	 * @param user 부모 사용자 엔티티
	 * @return 홈 화면 데이터 응답
	 */
	private HomeRes getParentHomeData(User user) {
		// 부모 전체 잔액 (Core 생기기 전까지 LocalBalanceProvider는 0 리턴)
		long parentBalance = balanceProvider.getUserTotalBalance(user);

		// 자녀 목록 + 각 자녀 잔액
		List<HomeRes.ChildDto> children = user.getChildren().stream()
			.map(UserRelationship::getChild)
			.map(child -> HomeRes.ChildDto.builder()
				.userId(child.getId())
				.name(child.getName())
				.balance(balanceProvider.getUserTotalBalance(child))
				.gender(child.getGender())
				.build())
			.toList();

		return HomeRes.builder()
			.user(HomeRes.UserDto.builder()
				.userId(user.getId())
				.name(user.getName())
				.role(user.getRole())
				.email(user.getEmail())
				.balance(parentBalance)
				.children(children)
				.build())
			.build();
	}

	/**
	 * 자녀 사용자의 홈 화면 데이터를 생성합니다.
	 * 계좌 타입별(용돈, 투자, 저축) 잔액 정보를 포함합니다.
	 *
	 * @param user 자녀 사용자 엔티티
	 * @return 홈 화면 데이터 응답
	 */
	private HomeRes getChildHomeData(User user) {
		long totalBalance = balanceProvider.getUserTotalBalance(user);

		return HomeRes.builder()
			.user(HomeRes.UserDto.builder()
				.userId(user.getId())
				.name(user.getName())
				.role(user.getRole())
				.email(user.getEmail())
				.totalBalance(totalBalance)
				.depositBalance(balanceProvider.getUserBalanceByType(user, AccountType.ALLOWANCE))
				.investmentBalance(balanceProvider.getUserBalanceByType(user, AccountType.INVEST))
				.savingBalance(balanceProvider.getUserBalanceByType(user, AccountType.GOAL))
				.build())
			.build();
	}
}
