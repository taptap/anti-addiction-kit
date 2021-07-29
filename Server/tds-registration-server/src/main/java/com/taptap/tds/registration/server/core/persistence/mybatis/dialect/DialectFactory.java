package com.taptap.tds.registration.server.core.persistence.mybatis.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DialectFactory {

    private static final Map<DatabaseDriver, Dialect> DIALECT_MAP;

    static {
        Map<DatabaseDriver, Dialect> dialectMap = new HashMap<>();
        dialectMap.put(DatabaseDriver.MYSQL, new MySQLDialect());
        dialectMap.put(DatabaseDriver.H2, new H2Dialect());

        DIALECT_MAP = Collections.unmodifiableMap(dialectMap);
    }

    public static Dialect fromProductName(DataSource dataSource) {
        try {
            String productName = JdbcUtils
                    .commonDatabaseName(JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName").toString());
            DatabaseDriver database = DatabaseDriver.fromProductName(productName);
            Dialect dialect = DIALECT_MAP.get(database);
            if (dialect == null) {
                throw new RuntimeException("Can not find Dialect from product name " + productName);
            }
            return dialect;
        } catch (MetaDataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
