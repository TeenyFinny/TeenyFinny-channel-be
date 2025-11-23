package dev.syntax.domain.home.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.service.BalanceProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

	private final BalanceProvider balanceProvider;

	@Override
	public HomeRes getHomeData(UserContext context) {
		User user = context.getUser();  // 이미 children/parents fetch join 상태

		if (user.getRole() == Role.PARENT) {
			return getParentHomeData(user);
		} else {
			return getChildHomeData(user);
		}
	}

	/**
	 * 부모 사용자의 홈 화면 데이터를 생성합니다.
	 */
	private HomeRes getParentHomeData(User user) {

		long parentBalance = balanceProvider.getUserTotalBalance(user);

		// 자녀 userId 리스트 추출
		List<User> childrenUsers = user.getChildren().stream()
			.map(UserRelationship::getChild)
			.toList();

		List<Long> childIds = childrenUsers.stream()
			.map(User::getId)
			.toList();

		// 배치 조회
		Map<Long, Long> childBalances = balanceProvider.getBalancesForUsers(childIds);

		List<HomeRes.ChildDto> children = childrenUsers.stream()
			.map(child -> HomeRes.ChildDto.builder()
				.userId(child.getId())
				.name(child.getName())
				.gender(child.getGender())
				.balance(childBalances.getOrDefault(child.getId(), 0L))
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
	 */
	private HomeRes getChildHomeData(User user) {

		long totalBalance = balanceProvider.getUserTotalBalance(user);

		// 자녀 1명의 계좌 타입별 잔액을 한 번에 배치 조회
		Map<AccountType, Long> balances = balanceProvider.getBalancesByType(user);

		return HomeRes.builder()
			.user(HomeRes.UserDto.builder()
				.userId(user.getId())
				.name(user.getName())
				.role(user.getRole())
				.email(user.getEmail())
				.totalBalance(totalBalance)
				.depositBalance(balances.getOrDefault(AccountType.ALLOWANCE, 0L))
				.investmentBalance(balances.getOrDefault(AccountType.INVEST, 0L))
				.savingBalance(balances.getOrDefault(AccountType.GOAL, 0L))
				.build())
			.build();
	}
}
