package dev.syntax.domain.home.service;

import static dev.syntax.global.service.Utils.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreChildAccountInfoRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;

/**
 * 홈 화면 서비스 구현체
 * <p>
 * Core 서버로부터 계좌 정보를 조회하여 사용자 역할(부모/자녀)에 따라
 * 적절한 형식으로 가공하여 반환합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

	private final CoreAccountClient coreAccountClient;

	@Override
	public HomeRes getHomeData(UserContext context) {
		User user = context.getUser();
		CoreUserAccountListRes coreAccounts = coreAccountClient.getUserAccounts();

		return user.getRole() == Role.PARENT
			? buildParentHomeData(context, coreAccounts)
			: buildChildHomeData(user, coreAccounts);
	}

	/**
	 * 부모 사용자의 홈 화면 데이터를 생성합니다.
	 * <p>
	 * 부모 본인의 계좌 잔액과 자녀들의 계좌 잔액을 포함하여 반환합니다.
	 * Core 서버에서 받은 자녀 정보는 coreUserId 기준으로 매핑됩니다.
	 * </p>
	 *
	 * @param context 사용자 컨텍스트 (부모 정보 포함)
	 * @param coreAccounts Core 서버로부터 조회한 계좌 정보
	 * @return 부모 홈 화면 응답 DTO
	 */
	private HomeRes buildParentHomeData(UserContext context, CoreUserAccountListRes coreAccounts) {
		User parent = context.getUser();

		// 부모 본인의 총 잔액 계산
		BigDecimal parentBalance = sumAccountBalances(coreAccounts.accounts());

		// 자녀별 잔액 맵 생성 (coreUserId -> 총 잔액)
		Map<Long, BigDecimal> childBalanceMap = buildChildBalanceMap(coreAccounts.children());

		// 자녀 정보 DTO 리스트 생성
		List<HomeRes.ChildDto> children = buildChildDtoList(parent, childBalanceMap);

		return HomeRes.builder()
			.user(HomeRes.UserDto.builder()
				.userId(parent.getId())
				.name(parent.getName())
				.role(parent.getRole())
				.email(parent.getEmail())
				.balance(NumberFormattingService(parentBalance))
				.children(children)
				.build())
			.build();
	}

	/**
	 * 자녀 사용자의 홈 화면 데이터를 생성합니다.
	 * <p>
	 * 자녀의 총 잔액과 계좌 타입별(용돈/투자/목표저축) 잔액을 포함하여 반환합니다.
	 * </p>
	 *
	 * @param child 자녀 사용자 정보
	 * @param coreAccounts Core 서버로부터 조회한 계좌 정보
	 * @return 자녀 홈 화면 응답 DTO
	 */
	private HomeRes buildChildHomeData(User child, CoreUserAccountListRes coreAccounts) {
		// 총 잔액 계산
		BigDecimal totalBalance = sumAccountBalances(coreAccounts.accounts());

		// 계좌 타입별 잔액 맵 생성 (accountType -> 잔액)
		Map<String, BigDecimal> balancesByType = groupBalancesByAccountType(coreAccounts.accounts());

		return HomeRes.builder()
			.user(HomeRes.UserDto.builder()
				.userId(child.getId())
				.name(child.getName())
				.role(child.getRole())
				.email(child.getEmail())
				.totalBalance(NumberFormattingService(totalBalance))
				.depositBalance(formatBalance(balancesByType, "DEPOSIT"))
				.investmentBalance(formatBalance(balancesByType, "INVEST"))
				.savingBalance(formatBalance(balancesByType, "GOAL"))
				.build())
			.build();
	}

	/**
	 * 자녀별 잔액 맵을 생성합니다.
	 * <p>
	 * Core 서버의 children 정보를 기반으로 각 자녀의 coreUserId를 키로,
	 * 해당 자녀의 총 잔액을 값으로 하는 맵을 생성합니다.
	 * </p>
	 *
	 * @param children Core 서버로부터 받은 자녀 계좌 정보 리스트
	 * @return coreUserId -> 총 잔액 맵
	 */
	private Map<Long, BigDecimal> buildChildBalanceMap(List<CoreChildAccountInfoRes> children) {
		if (children == null) {
			return Map.of();
		}

		return children.stream()
			.collect(Collectors.toMap(
				CoreChildAccountInfoRes::userId,  // Core의 userId = Channel의 coreUserId
				child -> sumAccountBalances(child.accounts())
			));
	}

	/**
	 * 자녀 정보 DTO 리스트를 생성합니다.
	 * <p>
	 * Channel DB의 자녀 정보와 Core 서버의 잔액 정보를 결합하여
	 * 클라이언트에 반환할 자녀 DTO 리스트를 생성합니다.
	 * </p>
	 *
	 * @param parent 부모 사용자 정보
	 * @param childBalanceMap 자녀별 잔액 맵 (coreUserId -> 잔액)
	 * @return 자녀 정보 DTO 리스트
	 */
	private List<HomeRes.ChildDto> buildChildDtoList(User parent, Map<Long, BigDecimal> childBalanceMap) {
		return parent.getChildren().stream()
			.map(relationship -> {
				User child = relationship.getChild();
				BigDecimal balance = childBalanceMap.getOrDefault(child.getCoreUserId(), BigDecimal.ZERO);

				return HomeRes.ChildDto.builder()
					.userId(child.getId())
					.name(child.getName())
					.gender(child.getGender())
					.balance(NumberFormattingService(balance))
					.build();
			})
			.toList();
	}

	/**
	 * 계좌 타입별로 잔액을 그룹핑합니다.
	 * <p>
	 * 동일한 계좌 타입의 잔액들을 합산하여 맵으로 반환합니다.
	 * 예: DEPOSIT -> 120,000, GOAL -> 200,000
	 * </p>
	 *
	 * @param accounts 계좌 정보 리스트
	 * @return 계좌 타입 -> 총 잔액 맵
	 */
	private Map<String, BigDecimal> groupBalancesByAccountType(List<CoreAccountItemRes> accounts) {
		return accounts.stream()
			.collect(Collectors.groupingBy(
				CoreAccountItemRes::accountType,
				Collectors.reducing(BigDecimal.ZERO, CoreAccountItemRes::balance, BigDecimal::add)
			));
	}

	/**
	 * 계좌 목록의 총 잔액을 계산합니다.
	 *
	 * @param accounts 계좌 정보 리스트
	 * @return 총 잔액
	 */
	private BigDecimal sumAccountBalances(List<CoreAccountItemRes> accounts) {
		return accounts.stream()
			.map(CoreAccountItemRes::balance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * 특정 계좌 타입의 잔액을 포맷팅하여 반환합니다.
	 * <p>
	 * 해당 타입의 잔액이 없으면 "0"을 반환합니다.
	 * </p>
	 *
	 * @param balancesByType 계좌 타입별 잔액 맵
	 * @param accountType 조회할 계좌 타입
	 * @return 포맷팅된 잔액 문자열 (예: "120,000")
	 */
	private String formatBalance(Map<String, BigDecimal> balancesByType, String accountType) {
		return NumberFormattingService(balancesByType.getOrDefault(accountType, BigDecimal.ZERO));
	}
}
