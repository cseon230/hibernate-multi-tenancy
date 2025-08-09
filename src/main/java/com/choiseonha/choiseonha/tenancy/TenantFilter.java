package com.choiseonha.choiseonha.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * TenantFilter가 필요한 이유
 *
 * : 멀티테넌시의 핵심 흐름은 요청이 들어올 때 어떤 스키마를 쓸지 결정하여
 * 그 정보로 Hibernate가 데이터 소스/스키마를 선택 하는 것.
 *
 * 이 필터는 그 흐름의 첫 단계(테넌트ID 채집기) 역할을 한다.
 */

@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

    // OncePerRequestFilter: 요청당 한 번만 동작하는 스프링 표준 필터. 중복 호출 방지

    // 요청 헤더로 테넌트 ID를 받을 때 사용할 헤더 키
    // (예시) X-Tenant-ID: tenant1
    private static final String TENANT_HEADER = "X-Tenant-ID";

    // 연습용 유효 테넌트 목록
    private static final Set<String> VALID_TENANTS = Set.of("tenant1", "tenant2");

    // OncePerRequestFilter 가 호출하는 템플릿 메서드. 이 안에서 요청 가공/검사를 함.
    // request: 들어온 HTTP 요청
    // response: 나갈 HTTP 응답
    // filterChaing: 다음 필터 혹은 컨트롤러로 넘겨주는 체인
    @Override
    protected void doFilterInternal (
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantId = request.getHeader(TENANT_HEADER);

        // tenantId가 없거나 공백이면 400 Bad Reqeust 반환
        if (!StringUtils.hasText(tenantId)) {
            log.warn("Missing `{} header. Request rejected.", TENANT_HEADER);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "MISSING X-Tenant-ID header");
            return; // 더 이상 필터 체인 진행하지 않음
        }

        // 유효하지 않은 테넌트 ID면 404 Not Found
        if (!VALID_TENANTS.contains(tenantId)) {
            log.warn("Invalid tenant ID `{}. Request rejected. ", tenantId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid tenant ID");
            return;
        }

        // 정상 처리
        try {
            TenantContext.set(tenantId);
            log.debug("Tenant set : {}", tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // 스레드 재사용 이슈 방지
        }

    }
}
