package com.taptap.tds.registration.server.core.persistence.mybatis.dialect;

public class MySQLDialect implements Dialect {

    @Override
    public String getLimitString(String sql, int offset, int limit) {

        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 12);
        sqlBuilder.append(sql);
        sqlBuilder.append(" LIMIT ");
        if (offset > 0) {
            sqlBuilder.append(offset);
            sqlBuilder.append(',');
        }
        sqlBuilder.append(limit);

        return sqlBuilder.toString();
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return true;
    }
}
