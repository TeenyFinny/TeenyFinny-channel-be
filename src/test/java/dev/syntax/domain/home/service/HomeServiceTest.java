package dev.syntax.domain.home.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.service.BalanceProvider;

/**
 * HomeService 비즈니스 로직을 검증하는 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

	@InjectMocks
	private HomeServiceImpl homeService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BalanceProvider balanceProvider;

	/**
	 * 부모 사용자가 자녀가 없는 경우, 본인 정보와 잔액만 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("부모 - 자녀 없는 경우")
	void getHomeData_Parent_NoChildren() {
		// given
		Long userId = 1L;
		User user = User.builder()
			.id(userId)
			.name("이부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.children(new ArrayList<>())
			.build();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceProvider.getUserTotalBalance(user)).willReturn(50000L);

		// when
		HomeRes response = homeService.getHomeData(userId);

		// then
		assertThat(response.user().role()).isEqualTo(Role.PARENT);
		assertThat(response.user().balance()).isEqualTo(50000L);
		assertThat(response.user().children()).isEmpty();
	}

	/**
	 * 부모 사용자가 자녀가 있는 경우, 본인 정보와 자녀 목록(잔액 포함)이 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("부모 - 자녀 있는 경우")
	void getHomeData_Parent_WithChildren() {
		// given
		Long parentId = 1L;
		User parent = User.builder()
			.id(parentId)
			.name("김부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.build();

		User child1 = User.builder()
			.id(2L)
			.name("김티니")
			.gender((byte)1)
			.build();

		User child2 = User.builder()
			.id(3L)
			.name("김피니")
			.gender((byte)2)
			.build();

		UserRelationship rel1 = UserRelationship.builder()
			.parent(parent)
			.child(child1)
			.build();

		UserRelationship rel2 = UserRelationship.builder()
			.parent(parent)
			.child(child2)
			.build();

		List<UserRelationship> children = List.of(rel1, rel2);

		parent = User.builder()
			.id(parentId)
			.name("김부모")
			.role(Role.PARENT)
			.email("parent@teenyfinny.com")
			.children(children)
			.build();

		given(userRepository.findById(parentId)).willReturn(Optional.of(parent));

		given(balanceProvider.getUserTotalBalance(parent)).willReturn(100000L);
		given(balanceProvider.getUserTotalBalance(child1)).willReturn(10000L);
		given(balanceProvider.getUserTotalBalance(child2)).willReturn(5000L);

		// when
		HomeRes response = homeService.getHomeData(parentId);

		// then
		assertThat(response.user().role()).isEqualTo(Role.PARENT);
		assertThat(response.user().balance()).isEqualTo(100000L);

		assertThat(response.user().children()).hasSize(2);
		assertThat(response.user().children().get(0).name()).isEqualTo("김티니");
		assertThat(response.user().children().get(0).balance()).isEqualTo(10000L);

		assertThat(response.user().children().get(1).name()).isEqualTo("김피니");
		assertThat(response.user().children().get(1).balance()).isEqualTo(5000L);
	}

	/**
	 * 자녀 사용자의 경우, 타입별 잔액이 BalanceProvider에서 정확히 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("자녀의 경우")
	void getHomeData_Child() {
		// given
		Long childId = 2L;
		User child = User.builder()
			.id(childId)
			.name("김티니")
			.role(Role.CHILD)
			.email("child@teenyfinny.com")
			.build();

		given(userRepository.findById(childId)).willReturn(Optional.of(child));

		given(balanceProvider.getUserTotalBalance(child)).willReturn(10000L);
		given(balanceProvider.getUserBalanceByType(child, AccountType.ALLOWANCE)).willReturn(1000L);
		given(balanceProvider.getUserBalanceByType(child, AccountType.GOAL)).willReturn(9000L);
		given(balanceProvider.getUserBalanceByType(child, AccountType.INVEST)).willReturn(0L);

		// when
		HomeRes response = homeService.getHomeData(childId);

		// then
		assertThat(response.user().role()).isEqualTo(Role.CHILD);
		assertThat(response.user().totalBalance()).isEqualTo(10000L);
		assertThat(response.user().depositBalance()).isEqualTo(1000L);
		assertThat(response.user().savingBalance()).isEqualTo(9000L);
		assertThat(response.user().investmentBalance()).isEqualTo(0L);
	}
}
