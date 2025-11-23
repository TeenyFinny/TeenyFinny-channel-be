package dev.syntax.domain.home.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.service.BalanceProvider;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

	@InjectMocks
	private HomeServiceImpl homeService;

	@Mock
	private BalanceProvider balanceProvider;

	@Test
	@DisplayName("부모 - 자녀 없는 경우")
	void parent_no_children() {
		User user = User.builder()
			.id(1L)
			.name("이부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.children(new ArrayList<>())
			.build();

		UserContext context = new UserContext(user);

		given(balanceProvider.getUserTotalBalance(user)).willReturn(50000L);

		HomeRes response = homeService.getHomeData(context);

		assertThat(response.user().role()).isEqualTo(Role.PARENT);
		assertThat(response.user().balance()).isEqualTo(50000L);
		assertThat(response.user().children()).isEmpty();
	}

	@Test
	@DisplayName("부모 - 자녀 있는 경우")
	void parent_with_children() {

		// parent
		User parent = User.builder()
			.id(1L)
			.name("김부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.build();

		// children
		User child1 = User.builder().id(2L).name("김티니").gender((byte)1).build();
		User child2 = User.builder().id(3L).name("김피니").gender((byte)2).build();

		UserRelationship r1 = UserRelationship.builder().parent(parent).child(child1).build();
		UserRelationship r2 = UserRelationship.builder().parent(parent).child(child2).build();

		parent = User.builder()
			.id(parent.getId())
			.name(parent.getName())
			.role(parent.getRole())
			.email(parent.getEmail())
			.children(List.of(r1, r2))
			.build();

		UserContext context = new UserContext(parent);

		given(balanceProvider.getUserTotalBalance(parent)).willReturn(100000L);

		// ⭐ 자녀 배치 잔액 모킹
		given(balanceProvider.getBalancesForUsers(List.of(2L, 3L)))
			.willReturn(Map.of(
				2L, 10000L,
				3L, 5000L
			));

		HomeRes response = homeService.getHomeData(context);

		assertThat(response.user().balance()).isEqualTo(100000L);
		assertThat(response.user().children()).hasSize(2);
		assertThat(response.user().children().get(0).balance()).isEqualTo(10000L);
		assertThat(response.user().children().get(1).balance()).isEqualTo(5000L);
	}

	@Test
	@DisplayName("자녀의 경우 - 계좌 타입별 잔액 배치 조회")
	void child_balances() {

		User child = User.builder()
			.id(2L)
			.name("김티니")
			.role(Role.CHILD)
			.email("child@teenyfinny.com")
			.build();

		UserContext context = new UserContext(child);

		given(balanceProvider.getUserTotalBalance(child)).willReturn(10000L);

		// ⭐ 타입별 배치 모킹
		given(balanceProvider.getBalancesByType(child))
			.willReturn(Map.of(
				AccountType.ALLOWANCE, 1000L,
				AccountType.GOAL, 9000L,
				AccountType.INVEST, 0L
			));

		HomeRes response = homeService.getHomeData(context);

		assertThat(response.user().totalBalance()).isEqualTo(10000L);
		assertThat(response.user().depositBalance()).isEqualTo(1000L);
		assertThat(response.user().savingBalance()).isEqualTo(9000L);
		assertThat(response.user().investmentBalance()).isEqualTo(0L);
	}
}
