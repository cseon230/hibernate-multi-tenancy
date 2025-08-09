package com.choiseonha.choiseonha.tenancy;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hibernate 멀티테넌시(SCHEMA 전략) 설정을 스프링 부트 JPA 자동 구성에 "주입"하는 설정 클래스입니다.
 *
 *  이 클래스가 하는 일
 *   1) CurrentTenantResolver 를 스프링 빈으로 등록
 *      - Hibernate가 "지금 테넌트 식별자 뭐야?"라고 물을 때 호출되는 SPI 구현체
 *      - 내부에서 TenantContext.get() 으로 필터가 넣어둔 tenantId를 꺼내 반환
 *
 *   2) HibernatePropertiesCustomizer 빈을 제공
 *      - 스프링 부트가 EntityManagerFactory 를 만들기 직전에 이 커스터마이저를 호출
 *      - 여기서 Hibernate 속성(Map)에 멀티테넌시 관련 설정을 세팅
 *
 *  효과
 *   - 이 설정이 적용되면, Hibernate는 쿼리 실행/세션 생성 시점마다
 *     CurrentTenantResolver.resolveCurrentTenantIdentifier() 를 호출해 tenantId를 얻고,
 *     SchemaPerTenantConnectionProvider.getConnection(tenantId) 를 통해 해당 스키마로 전환합니다.
 */

@Configuration
public class HibernateMultiTenancyConfig {

    /**
     * CurrentTenantResolver 를 스프링 컨테이너에 등록합니다.
     *
     * 왜 @Bean 인가?
     *  - 이 클래스는 @Component 로 스캔하지 않고 명시적으로 한 개만(싱글턴) 등록하려는 목적
     *  - HibernatePropertiesCustomizer 에서 참조할 수 있도록 스프링 관리 하에 둡니다.
     *
     * 참고:
     *  - CurrentTenantResolver 는 org.hibernate.context.spi.CurrentTenantIdentifierResolver<String> 구현체여야 하며,
     *    내부에서 TenantContext.get() 값을 반환하도록 구현되어 있습니다.
     */
    @Bean
    public CurrentTenantResolver currentTenantResolver() {
        return new CurrentTenantResolver();
    }

    /**
     * Hibernate 속성을 커스터마이징합니다.
     *
     * 스프링 부트 동작 순서 요약:
     *  - Spring Boot JPA AutoConfig → EntityManagerFactory 생성 직전
     *    → 모든 HibernatePropertiesCustomizer 를 호출해 hibernatePropertyMap 을 수정
     *    → 수정된 속성으로 Hibernate 부팅
     *
     * 여기서 세팅하는 핵심 속성 3가지:
     *  1) hibernate.multiTenancy = "SCHEMA"
     *       - 멀티테넌시 전략을 "스키마 분리"로 활성화 (DATABASE/SCHEMA/DISCRIMINATOR 중 SCHEMA)
     *
     *  2) hibernate.multi_tenant_connection_provider = connectionProvider
     *       - 테넌트별 커넥션 제공자 등록 (SchemaPerTenantConnectionProvider)
     *       - Hibernate가 커넥션을 요구할 때 getConnection(tenantId) 호출 → 내부에서 USE `<tenantId>`
     *
     *  3) hibernate.tenant_identifier_resolver = tenantResolver
     *       - 현재 테넌트 식별자 제공자 등록 (CurrentTenantResolver)
     *       - Hibernate가 "지금 테넌트?"를 물을 때 resolveCurrentTenantIdentifier() 호출
     *
     * 주의:
     *  - 아래 key 들은 문자열로 넣었지만, org.hibernate.cfg.AvailableSettings 상수를 써도 동일하게 동작합니다.
     *  - 현재 프로젝트/IDE 환경에서 import 이슈가 있을 경우 문자열 키 사용이 가장 간단합니다.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer (
            SchemaPerTenantConnectionProvider connectionProvider,
            CurrentTenantResolver tenantResolver
    ) {
        return (props) -> {
            props.put("hibernate.multiTenancy", "SCHEMA"); // 멀티테넌시 전략 활성화: SCHEMA
            props.put("hibernate.multi_tenant_connection_provider", connectionProvider); // 커넥션 제공자: tenantId에 맞춰 스키마 전환 (USE `<tenantId>`)
            props.put("hibernate.tenant_identifier_resolver", tenantResolver); // 테넌트 식별자 리졸버: ThreadLocal(TenantContext)에 저장된 tenantId 반환
        };
    }
}
