package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import com.taptap.tds.registration.server.core.persistence.mybatis.util.MybatisUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.query.parser.PartTree;


public class BaseSelectProvider extends SqlProviderSupport {

    public static CharSequence findOne(ProviderContext context, String selectListExpression, String joinExpression,
            Boolean forUpdate) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);

        StringBuilder sqlBuilder = getBaseSelectBuilder(entityInformation, selectListExpression, joinExpression);
        sqlBuilder.append(buildPrimaryKeysWhereClause(entityInformation, ID_PARAM_NAME));
        appendForUpdate(sqlBuilder, forUpdate);

        return sqlBuilder;
    }

    public static CharSequence findAllByIds(ProviderContext context, Iterable<?> ids, String selectListExpression,
            String joinExpression, Boolean forUpdate) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);

        StringBuilder sqlBuilder = getBaseSelectBuilder(entityInformation, selectListExpression, joinExpression);
        sqlBuilder.append(buildPrimaryKeysInWhereClause(entityInformation, ids));
        appendForUpdate(sqlBuilder, forUpdate);

        return sqlBuilder;
    }

    public static CharSequence findAll(ProviderContext context, FieldsExpand fieldsExpand) {

        String selectListExpression = null;
        String joinExpression = null;
        Boolean forUpdate = null;
        if (fieldsExpand != null) {
            selectListExpression = fieldsExpand.getSelectListExpression();
            joinExpression = fieldsExpand.getJoinExpression();
            forUpdate = fieldsExpand.getForUpdate();
        }

        EntityInformation entityInformation = getCurrentEntityInformation(context);

        StringBuilder sqlBuilder = getBaseSelectBuilder(entityInformation, selectListExpression, joinExpression);
        appendLogicalDeletionWhereClause(sqlBuilder, entityInformation, true);
        appendForUpdate(sqlBuilder, forUpdate);

        return sqlBuilder;
    }

    public static CharSequence findByCondition(ProviderContext context, Object parameterObject) {

        String selectListExpression = MybatisUtils.extractMapperMethodParameter(parameterObject, "selectListExpression");
        String joinExpression = MybatisUtils.extractMapperMethodParameter(parameterObject, "joinExpression");
        Boolean forUpdate = MybatisUtils.extractMapperMethodParameter(parameterObject, "forUpdate");

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        PartTree partTree = new PartTree(context.getMapperMethod().getName(), entityInformation.getEntityClass());

        StringBuilder sqlBuilder = getBaseSelectBuilder(entityInformation, selectListExpression, joinExpression);
        sqlBuilder.append(buildWhereClause(entityInformation, partTree, parameterObject));
        if (partTree.getSort().isSorted()) {
            sqlBuilder.append(" ORDER BY ");
            boolean first = true;
            for (Order order : partTree.getSort()) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                String column = entityInformation.getCorrespondingColumn(order.getProperty());
                sqlBuilder.append(entityInformation.getTableName());
                sqlBuilder.append('.');
                sqlBuilder.append(column);
                sqlBuilder.append(' ');
                sqlBuilder.append(order.getDirection());
            }
        }
        if (partTree.getMaxResults() != null) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(partTree.getMaxResults());
        }
        appendForUpdate(sqlBuilder, forUpdate);

        return sqlBuilder;
    }

    private static StringBuilder getBaseSelectBuilder(EntityInformation entityInformation, String selectListExpression,
            String joinExpression) {

        if (StringUtils.isEmpty(selectListExpression)) {
            selectListExpression = entityInformation.getSelectListExpression();
        }

        StringBuilder sqlBuilder = new StringBuilder(128);
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(selectListExpression);
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(entityInformation.getTableName());
        if (joinExpression != null) {
            sqlBuilder.append(joinExpression);
        }

        return sqlBuilder;
    }

    private static void appendForUpdate(StringBuilder sqlBuilder, Boolean forUpdate) {
        if (forUpdate != null && forUpdate) {
            sqlBuilder.append(" FOR UPDATE");
        }
    }

    public static CharSequence exists(ProviderContext context) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getBaseExistsSqlBuilder(entityInformation);
        sqlBuilder.append(buildPrimaryKeysWhereClause(entityInformation, ID_PARAM_NAME));

        return sqlBuilder;
    }

    public static CharSequence existsByCondition(ProviderContext context, Object parameterObject) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getBaseExistsSqlBuilder(entityInformation);
        sqlBuilder.append(buildWhereClause(entityInformation, context.getMapperMethod(), parameterObject));

        return sqlBuilder;
    }

    private static StringBuilder getBaseExistsSqlBuilder(EntityInformation entityInformation) {

        StringBuilder sqlBuilder = new StringBuilder(128);
        sqlBuilder.append("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM ");
        sqlBuilder.append(entityInformation.getTableName());

        return sqlBuilder;
    }

    public static CharSequence count(ProviderContext context) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getBaseCountSqlBuilder(entityInformation);
        appendLogicalDeletionWhereClause(sqlBuilder, entityInformation, true);

        return sqlBuilder;
    }

    public static CharSequence countByCondition(ProviderContext context, Object parameterObject) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        StringBuilder sqlBuilder = getBaseCountSqlBuilder(entityInformation);
        sqlBuilder.append(buildWhereClause(entityInformation, context.getMapperMethod(), parameterObject));

        return sqlBuilder;
    }

    private static StringBuilder getBaseCountSqlBuilder(EntityInformation entityInformation) {

        StringBuilder sqlBuilder = new StringBuilder(32);
        sqlBuilder.append("SELECT COUNT(*) FROM ");
        sqlBuilder.append(entityInformation.getTableName());

        return sqlBuilder;
    }
}
