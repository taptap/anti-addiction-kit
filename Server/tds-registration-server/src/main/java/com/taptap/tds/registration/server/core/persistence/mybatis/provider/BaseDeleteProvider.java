package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;

public class BaseDeleteProvider extends SqlProviderSupport {

    public static CharSequence delete(ProviderContext context) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getDeletionSqlBuilder(entityInformation);
        CharSequence whereSql = buildPrimaryKeysWhereClause(entityInformation, ID_PARAM_NAME);
        sqlBuilder.append(whereSql);

        return sqlBuilder;
    }

    private static StringBuilder getDeletionSqlBuilder(EntityInformation entityInformation) {
        if (entityInformation.getLogicalDeletionColumn() == null) {
            return getBaseDeletionSqlBuilder(entityInformation);
        } else {
            return getLogicalDeletionSqlBuilder(entityInformation);
        }
    }

    private static StringBuilder getBaseDeletionSqlBuilder(EntityInformation entityInformation) {
        StringBuilder sqlBuilder = new StringBuilder(32);
        sqlBuilder.append("DELETE FROM ");
        sqlBuilder.append(entityInformation.getTableName());
        return sqlBuilder;
    }

    private static StringBuilder getLogicalDeletionSqlBuilder(EntityInformation entityInformation) {
        StringBuilder sqlBuilder = new StringBuilder(64);
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(entityInformation.getTableName());
        sqlBuilder.append(" SET ");
        sqlBuilder.append(entityInformation.getLogicalDeletionColumn());
        sqlBuilder.append(" = 1");
//        if (entityInformation.isUpdatedByPresent()) {
//            sqlBuilder.append(", ");
//            appendWhereClause(sqlBuilder, "updated_by", "updatedBy");
//        }
        return sqlBuilder;
    }

    public static CharSequence bulkDelete(ProviderContext context, @Param("collection") Iterable<?> ids) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getDeletionSqlBuilder(entityInformation);
        CharSequence whereSql = buildPrimaryKeysInWhereClause(entityInformation, ids, "collection");
        sqlBuilder.append(whereSql);

        return sqlBuilder;
    }

    public static CharSequence deleteByCondition(ProviderContext context, Object parameterObject) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getDeletionSqlBuilder(entityInformation);
        CharSequence whereSql = buildWhereClause(entityInformation, context.getMapperMethod(), parameterObject);
        sqlBuilder.append(whereSql);

        return sqlBuilder;
    }
}
