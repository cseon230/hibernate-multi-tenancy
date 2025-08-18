package com.choiseonha.choiseonha.tenancy;

public class TenantContext {

    /**
     * 멀티테넌시에서 “이번 요청은 어떤 테넌트(스키마)를 대상으로 해야 하는가?”를 매 요청마다 저장/조회/초기화하기 위한 요청 범위 저장소.
     * 스프링 MVC는 요청을 처리할 때 스레드 풀에서 스레드 하나를 꺼내 컨트롤러까지 쭉 실행한다.
     * 이때 "이번 요청의 테넌트ID"를 전역(static) 변수에 두면 스레드 끼리 섞이기 때문에
     * ThreadLocal을 사용하여 "같은 스레드 안에서만 보이는 저장 공간" 을 만들어 요청 처리 동안 안전하게 보관할 수 있다.
     */

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
