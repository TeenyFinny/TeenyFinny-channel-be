package dev.syntax.domain.home.service;

import java.util.List;

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
