package dev.syntax.domain.transfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoTransferInquiryServiceImpl implements AutoTransferInquiryService {

    private final AutoTransferRepository autoTransferRepository;
    /**
     * 자동이체 설정 조회.
     *
     * - 부모 권한 검증
     * - 자녀 ID로 자동이체 설정 최대 2건(용돈+투자/목표 총 2개) 조회
     * - 없으면 isInit=true 반환
     * - 있으면 설정 값을 포함하여 isInit=false 반환
     */
    @Override
    public AutoTransferRes getAutoTransfer(Long childId, AutoTransferType type, UserContext ctx) {

        validateParentAccess(ctx, childId);

        // 자녀별 + 타입별 자동이체 설정 조회
        return autoTransferRepository.findByUserIdAndType(childId, type)
                .map(transfer -> {
                    Integer ratio = transfer.getRatio();
                    // GOAL 타입일 경우 비율 정보는 불필요하므로 null 처리 (또는 0)
                    if (type == AutoTransferType.GOAL) {
                        ratio = null;
                    }
                    return AutoTransferRes.of(
                            transfer.getId(),
                            transfer.getTransferAmount(),
                            transfer.getTransferDate(),
                            ratio
                    );
                })
                .orElseGet(AutoTransferRes::init);
    }

    /**
     * 부모 접근 권한 검증
     */
    private void validateParentAccess(UserContext ctx, Long childId) {

        // 1. 부모 권한 확인
        if (!Role.PARENT.name().equals(ctx.getRole())) {
            throw new BusinessException(ErrorBaseCode.PARENT_ONLY_FEATURE);
        }

        // 2. 부모의 자녀인지 검증
        if (!ctx.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
    }
}
