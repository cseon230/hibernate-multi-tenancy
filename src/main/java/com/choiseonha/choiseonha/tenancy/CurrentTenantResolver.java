package com.choiseonha.choiseonha.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.util.StringUtils;

/**
 * Hibernate가 현재 테넌트를 알아낼 때 호출하는 리졸버.
 * 헤더 -> TenantFilter -> TenantContext 에 저장된 TenantId를 읽어서 반환함
 * 헤더가 없을 땐 기본 테넌트로 fallback
 */

public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.get();

        // 필터에서 이미 유효성 검증을 했지만, 이중 안전장치로 null/blank 차단
        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalStateException("No tenantId found in TenantContext. Request may be missing required header.");
        }

        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // true: 현재 세션이 있어도 테넌트 식별자 재검증 허용
        return true;
    }
}
