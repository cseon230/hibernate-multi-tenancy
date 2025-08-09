package com.choiseonha.choiseonha.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.util.StringUtils;

/**
 * Hibernate가 현재 테넌트를 알아낼 때 호출하는 리졸버.
 * 헤더 -> TenantFilter -> TenantContext 에 저장된 TenantId를 읽어서 반환함
 * 헤더가 없을 땐 기본 테넌트로 fallback
 */

public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<String> {

    // 기본 테넌트: 지금은 RDS에 연결한 기본 DB명과 동일하게 두면 안전.
    // (application.yml의 기본 DB가 firstRds 이므로 아래와 같이 둠
    private static final String DEFAULT_TENANT = "firstRds";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.get();
        return StringUtils.hasText(tenantId) ? tenantId : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // true: 현재 세션이 있어도 테넌트 식별자 재검증 허용
        return true;
    }
}
