package com.taptap.tds.registration.server.core.persistence.mybatis.dialect;

public class H2Dialect implements Dialect {

    @Override
    public String getLimitString(String sql, int offset, int limit) {

        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
        sqlBuilder.append(sql);
        sqlBuilder.append(" LIMIT ");
        sqlBuilder.append(limit);
        if (offset > 0) {
            sqlBuilder.append(" OFFSET ");
            sqlBuilder.append(offset);
        }

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
