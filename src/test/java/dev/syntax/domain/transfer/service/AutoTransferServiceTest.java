package dev.syntax.domain.transfer.service;

import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class AutoTransferServiceTest {

    @Mock
    private AutoTransferRepository autoTransferRepository;

    @InjectMocks
    private AutoTransferInquiryServiceImpl service;

    private UserContext parentCtx;
    private UserContext childCtx;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 부모 User 생성
        User parent = User.builder()
            .id(1L)
            .name("이부모")
            .role(Role.PARENT)
            .email("parent@teenyfinny.com")
            .build();

        // 자녀 User 생성 (childId = 10L)
        User childEntity = User.builder()
            .id(10L)
            .name("김티니")
            .role(Role.CHILD)
            .email("child10@teenyfinny.com")
            .build();

        UserRelationship r1 = UserRelationship.builder()
            .parent(parent)
            .child(childEntity)
            .build();

        parent = User.builder()
            .id(parent.getId())
            .name(parent.getName())
            .role(parent.getRole())
            .email(parent.getEmail())
            .children(new ArrayList<>(List.of(r1)))
            .build();

        /**
         * 부모 컨텍스트 생성 → children 목록에도 childId 포함됨
         */
        parentCtx = new UserContext(parent);


        /**
         * 자녀 컨텍스트 (부모 아님)
         */
        User child = User.builder()
            .id(2L)
            .name("다른자녀")
            .role(Role.CHILD)
            .email("child@teenyfinny.com")
            .build();

        childCtx = new UserContext(child);
    }

    @Test
    @DisplayName("부모가 아닌 사용자는 조회 불가")
    void fail_if_not_parent() {
        // given
        Long childId = 10L;

        // when / then
        assertThatThrownBy(() -> service.getAutoTransfer(childId, childCtx))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.PARENT_ONLY_FEATURE.getMessage());
    }

    @Test
    @DisplayName("부모라도 자신의 자녀가 아니면 조회 불가")
    void fail_if_not_own_child() {
        // given
        Long notMyChildId = 999L;

        // when / then
        assertThatThrownBy(() -> service.getAutoTransfer(notMyChildId, parentCtx))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.INVALID_CHILD.getMessage());
    }

    @Test
    @DisplayName("자동이체 설정이 없는 경우 isInit=true 반환")
    void return_init_when_no_auto_transfer() {
        // given
        Long childId = 10L;

        given(autoTransferRepository.findByUserId(childId))
                .willReturn(Optional.empty());

        // when
        AutoTransferRes res = service.getAutoTransfer(childId, parentCtx);

        // then
        assertThat(res.getIsInit()).isTrue();
        assertThat(res.getTransferId()).isNull();
        assertThat(res.getTransferAmount()).isNull();
        assertThat(res.getTransferDate()).isNull();
    }

    @Test
    @DisplayName("자동이체가 있는 경우 isInit=false + 저장된 값 반환")
    void return_auto_transfer_when_exists() {
        // given
        Long childId = 10L;

        AutoTransfer transfer = AutoTransfer.builder()
                .id(1L)
                .user(null)
                .account(null)
                .transferAmount(BigDecimal.valueOf(25000))
                .transferDate(14)
                .ratio(25)
                .type(AutoTransferType.ALLOWANCE)
                .build();

        given(autoTransferRepository.findByUserId(childId))
                .willReturn(Optional.of(transfer));

        // when
        AutoTransferRes res = service.getAutoTransfer(childId, parentCtx);

        // then
        assertThat(res.getIsInit()).isFalse();
        assertThat(res.getTransferId()).isEqualTo(1L);
        assertThat(res.getTransferAmount()).isEqualTo("25,000");
        assertThat(res.getTransferDate()).isEqualTo(14);
        assertThat(res.getRatio()).isEqualTo(25);
    }
}
