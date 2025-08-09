package com.choiseonha.choiseonha.tenancy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 테넌트 식별자(=스키마명)에 따라 커넥션을 해당 스키마로 전환하는 Provider.
 * MariaDB/MySQL 계열은 "USE <schema>" 로 스키마 전환을 수행함.
 * 요청 끝에 releaseConnection에서 기본 스키마로 복귀(선택) 후 close 함.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaPerTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    // Hibernate가 커넥션을 얻을 때 마다 이 클래스를 거친다.
    // MultiTenantConnectionProvider: 테넌트별로 커넥션을 주는 역할

    private static final String DEFAULT_SCHEMA = "firstRds"; // application.yml의 기본 DB명과 동일

    private final DataSource dataSource; // DB 연결을 관리하는 객체. 커넥션은 DB에 직접 연결해야 하므로 기본 연결 소스(DataSource)가 필요함.
    // DataSource를 주입받아서
    // 1. getAnyConnection() -> dataSource.getConnection() 호출
    // 2. getConnection(tenantIdentifier) -> dataSource.getConnection() + USE <schema> 실행

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        // TenantFilter 에서 이미 검증했지만 한 번 더 체크
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            throw new SQLException("Missing tenant identifier");
        }

        Connection connection = getAnyConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE `" + tenantIdentifier + "`");
            log.debug("Switched schema to {}", tenantIdentifier);
            return connection;
        } catch (SQLException e) {
            try { connection.close(); } catch (SQLException ignore) {}
            throw new SQLException("Could not switch to schema " + tenantIdentifier, e);
        }
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        // 기본 스키마로 복귀 로직 제거함. 정책상 잘못된 요청은 상단에서 이미 차단했기 때문.
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false; // 커넥션을 공격적으로 반납하지 않음
    }

    @Override
    public boolean isUnwrappableAs(@NonNull Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(@NonNull Class<T> unwrapType) {
        return null;
    }
}
