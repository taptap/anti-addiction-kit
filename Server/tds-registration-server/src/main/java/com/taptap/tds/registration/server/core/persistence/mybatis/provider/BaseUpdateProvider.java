package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.repository.query.parser.UpdatePartTree;

import java.util.Map;

public class BaseUpdateProvider extends SqlProviderSupport {

    public static CharSequence update(ProviderContext context, Object entity) {
        return generateUpdateSql(context, entity, false);
    }

    public static CharSequence updateNonNull(ProviderContext context, Object entity) {
        return generateUpdateSql(context, entity, true);
    }

    private static CharSequence generateUpdateSql(ProviderContext context, Object entity, boolean nonNull) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        ConfigurablePropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);

        StringBuilder sqlBuilder = new StringBuilder(128);
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(entityInformation.getTableName());
        sqlBuilder.append(" SET ");

        boolean first = true;
        for (Map.Entry<String, String> entry : entityInformation.getUpdatableColumns().entrySet()) {
            // indicates non-null mode
            if (nonNull) {
                Object propertyValue = propertyAccessor.getPropertyValue(entry.getValue());
                if (propertyValue == null) {
                    continue;
                }
            }
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(',');
            }
            sqlBuilder.append(entityInformation.getTableName());
            sqlBuilder.append('.');
            sqlBuilder.append(entry.getKey());
            sqlBuilder.append(" = #{");
            sqlBuilder.append(entry.getValue());
            sqlBuilder.append('}');
        }

        CharSequence whereSql = buildPrimaryKeysWhereClause(entityInformation);
        sqlBuilder.append(whereSql);
        appendVersionWhereClause(sqlBuilder, entityInformation, entity);

        return sqlBuilder;
    }

    public static CharSequence updateByCondition(ProviderContext context, Object parameterObject) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);

        UpdatePartTree partTree = new UpdatePartTree(context.getMapperMethod().getName(), entityInformation);

        StringBuilder sqlBuilder = new StringBuilder(128);
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(entityInformation.getTableName());
        sqlBuilder.append(" SET ");

        boolean first = true;
        for (String property : partTree.getProjections()) {
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append(entityInformation.getTableName());
            sqlBuilder.append('.');
            sqlBuilder.append(entityInformation.getCorrespondingColumn(property));
            sqlBuilder.append(" = #{");
            sqlBuilder.append(property);
            sqlBuilder.append('}');
        }
//        if (entityInformation.isUpdatedByPresent()) {
//            sqlBuilder.append(", ");
//            sqlBuilder.append(entityInformation.getTableName());
//            sqlBuilder.append(".updated_by = #{updatedBy}");
//        }

        CharSequence whereSql = buildWhereClause(entityInformation, partTree, parameterObject);
        sqlBuilder.append(whereSql);
        appendVersionWhereClause(sqlBuilder, entityInformation, parameterObject);

        return sqlBuilder;
    }

    private static void appendVersionWhereClause(StringBuilder sqlBuilder, EntityInformation entityInformation,
            Object parameter) {

        if (!entityInformation.isVersioning()) {
            return;
        }
        Object version = null;
        if (parameter instanceof ConfigurablePropertyAccessor) {
            ConfigurablePropertyAccessor propertyAccessor = (ConfigurablePropertyAccessor) parameter;
            version = propertyAccessor.getPropertyValue(entityInformation.getVersionProperty());
        } else if (parameter instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) parameter;
            if (paramMap.containsKey(entityInformation.getVersionProperty())) {
                version = paramMap.get(entityInformation.getVersionProperty());
            }
        } else {
            ConfigurablePropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(parameter);
            version = propertyAccessor.getPropertyValue(entityInformation.getVersionProperty());
        }
        if (version == null) {
            return;
        }

        sqlBuilder.append(" AND ");
        appendWhereClause(sqlBuilder, entityInformation.getCorrespondingColumn(entityInformation.getVersionProperty()),
                entityInformation.getVersionProperty());
    }
}
