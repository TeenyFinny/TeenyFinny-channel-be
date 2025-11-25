package dev.syntax.domain.home.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreChildAccountInfoRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

	@InjectMocks
	private HomeServiceImpl homeService;

	@Mock
	private CoreAccountClient coreAccountClient;

	@Test
	@DisplayName("부모 - 자녀 없는 경우")
	void parent_no_children() {

		User parent = User.builder()
			.id(1L)
			.name("이부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.children(new ArrayList<>())
			.build();

		UserContext context = new UserContext(parent);

		// Core 계좌 조회 Mock — 파라미터 없음
		CoreUserAccountListRes coreAccounts = new CoreUserAccountListRes(
			List.of(
				new CoreAccountItemRes(1L, "1234-5678", "DEPOSIT", new BigDecimal("50000"))
			),
			List.of()
		);
		given(coreAccountClient.getUserAccounts()).willReturn(coreAccounts);

		HomeRes res = homeService.getHomeData(context);

		assertThat(res.user().role()).isEqualTo(Role.PARENT);
		assertThat(res.user().balance()).isEqualTo("50,000");
		assertThat(res.user().children()).isEmpty();
	}

	@Test
	@DisplayName("부모 - 자녀 있는 경우")
	void parent_with_children() {

		User parent = User.builder()
			.id(1L)
			.name("김부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.build();

		// children
		User child1 = User.builder().id(2L).name("김티니").gender((byte)1).coreUserId(2L).build();
		User child2 = User.builder().id(3L).name("김피니").gender((byte)2).coreUserId(3L).build();

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

		// Core 계좌 조회 Mock
		CoreUserAccountListRes coreAccounts = new CoreUserAccountListRes(
			List.of(
				new CoreAccountItemRes(1L, "1234-5678", "DEPOSIT", new BigDecimal("60000")),
				new CoreAccountItemRes(2L, "1234-5679", "INVEST", new BigDecimal("40000"))
			),
			List.of(
				new CoreChildAccountInfoRes(2L, List.of(
					new CoreAccountItemRes(3L, "2234-5678", "DEPOSIT", new BigDecimal("10000"))
				)),
				new CoreChildAccountInfoRes(3L, List.of(
					new CoreAccountItemRes(4L, "3234-5678", "DEPOSIT", new BigDecimal("5000"))
				))
			)
		);

		given(coreAccountClient.getUserAccounts()).willReturn(coreAccounts);

		HomeRes res = homeService.getHomeData(context);

		assertThat(res.user().balance()).isEqualTo("100,000");
		assertThat(res.user().children()).hasSize(2);
		assertThat(res.user().children().get(0).balance()).isEqualTo("10,000");
		assertThat(res.user().children().get(1).balance()).isEqualTo("5,000");
	}

	@Test
	@DisplayName("자녀 - 계좌 타입별 잔액")
	void child_balances() {

		User child = User.builder()
			.id(2L)
			.name("김티니")
			.role(Role.CHILD)
			.email("child@teenyfinny.com")
			.build();

		UserContext context = new UserContext(child);

		CoreUserAccountListRes coreAccounts = new CoreUserAccountListRes(
			List.of(
				new CoreAccountItemRes(1L, "1234-5678", "DEPOSIT", new BigDecimal("1000")),
				new CoreAccountItemRes(2L, "1234-5679", "GOAL", new BigDecimal("9000"))
			),
			List.of()
		);

		given(coreAccountClient.getUserAccounts()).willReturn(coreAccounts);

		HomeRes res = homeService.getHomeData(context);

		assertThat(res.user().totalBalance()).isEqualTo("10,000");
		assertThat(res.user().depositBalance()).isEqualTo("1,000");
		assertThat(res.user().savingBalance()).isEqualTo("9,000");
		assertThat(res.user().investmentBalance()).isEqualTo("0");
	}
}
