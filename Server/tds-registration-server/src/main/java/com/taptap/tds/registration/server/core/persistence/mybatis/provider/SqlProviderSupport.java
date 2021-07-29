package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import com.taptap.tds.registration.server.core.persistence.EntityInformationFactory;
import com.taptap.tds.registration.server.core.persistence.mybatis.util.MybatisUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.repository.query.parser.PartTree.OrPart;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SqlProviderSupport {

    public static final String GENERAL_PARAM_NAME_PREFIX = "param";

    protected static final String ID_PARAM_NAME = "id";

    private static final ConcurrentMap<Class<?>, EntityInformation> MAPPER_STATEMENT_TYPE_CACHE = new ConcurrentHashMap<>();

    protected static EntityInformation getCurrentEntityInformation(ProviderContext providerContext) {
        return MAPPER_STATEMENT_TYPE_CACHE.computeIfAbsent(providerContext.getMapperType(), mapperType -> {
            Class<?> entityClass = MybatisUtils.getMapperEntityType(mapperType);
            return EntityInformationFactory.getEntityInformation(entityClass);
        });
    }

    protected static CharSequence buildPrimaryKeysWhereClause(EntityInformation entityInformation) {
        return buildPrimaryKeysWhereClause(entityInformation, null);
    }

    protected static CharSequence buildPrimaryKeysWhereClause(EntityInformation entityInformation, String paramName) {

        boolean compositePrimaryKeys = entityInformation.getIdMetadata().isCompositePrimaryKeys();
        boolean first = true;
        StringBuilder sqlBuilder = new StringBuilder(128);

        for (Map.Entry<String, String> entry : entityInformation.getIdColumnPropertyMap().entrySet()) {
            if (first) {
                first = false;
                sqlBuilder.append(" WHERE ");
            } else {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append(entityInformation.getTableName());
            sqlBuilder.append('.');
            if (compositePrimaryKeys) {
                paramName = GENERAL_PARAM_NAME_PREFIX + "1." + entry.getValue();
            } else if (StringUtils.isEmpty(paramName)) {
                paramName = entry.getValue();
            }
            paramName = compositePrimaryKeys ? GENERAL_PARAM_NAME_PREFIX + "1." + entry.getValue() : paramName;
            appendWhereClause(sqlBuilder, entry.getKey(), paramName);
        }

        appendLogicalDeletionWhereClause(sqlBuilder, entityInformation, false);

        return sqlBuilder;
    }

    protected static void appendLogicalDeletionWhereClause(StringBuilder sqlBuilder, EntityInformation entityInformation,
            boolean appendWherePrefix) {

        String logicalDeletionColumn = entityInformation.getLogicalDeletionColumn();
        if (StringUtils.isNotEmpty(logicalDeletionColumn)) {
            if (appendWherePrefix) {
                sqlBuilder.append(" WHERE ");
            } else {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append(entityInformation.getTableName());
            sqlBuilder.append('.');
            sqlBuilder.append(logicalDeletionColumn);
            sqlBuilder.append(" = 0");
        }
    }

    protected static StringBuilder buildPrimaryKeysInWhereClause(EntityInformation entityInformation, Iterable<?> ids) {
        return buildPrimaryKeysInWhereClause(entityInformation, ids, null);
    }

    protected static StringBuilder buildPrimaryKeysInWhereClause(EntityInformation entityInformation, Iterable<?> ids,
            String paramName) {

        StringBuilder sqlBuilder = new StringBuilder(128);

        Map<String, String> idColumnPropertyMap = entityInformation.getIdColumnPropertyMap();
        boolean compositePrimaryKeys = entityInformation.getIdMetadata().isCompositePrimaryKeys();
        sqlBuilder.append(" WHERE ");
        if (compositePrimaryKeys) {
            sqlBuilder.append('(');
        }

        boolean simpleValueType = BeanUtils.isSimpleValueType(ids.iterator().next().getClass());
        boolean first = true;
        for (String keyColumn : idColumnPropertyMap.keySet()) {
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(',');
            }
            sqlBuilder.append(entityInformation.getTableName());
            sqlBuilder.append('.');
            sqlBuilder.append(keyColumn);
        }
        if (compositePrimaryKeys) {
            sqlBuilder.append(')');
        }
        sqlBuilder.append(" IN (");
        int size = IterableUtils.size(ids);
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sqlBuilder.append(',');
            }
            if (simpleValueType) {
                sqlBuilder.append("#{");
                if (paramName != null) {
                    sqlBuilder.append(paramName);
                } else {
                    sqlBuilder.append(GENERAL_PARAM_NAME_PREFIX);
                    sqlBuilder.append('1');
                }
                sqlBuilder.append('[');
                sqlBuilder.append(i);
                sqlBuilder.append(']');
                sqlBuilder.append('}');
            } else {
                sqlBuilder.append('(');
                first = true;
                for (String keyProperty : idColumnPropertyMap.values()) {
                    if (first) {
                        first = false;
                    } else {
                        sqlBuilder.append(',');
                    }
                    sqlBuilder.append("#{");
                    if (paramName != null) {
                        sqlBuilder.append(paramName);
                    } else {
                        sqlBuilder.append(GENERAL_PARAM_NAME_PREFIX);
                        sqlBuilder.append('1');
                    }
                    sqlBuilder.append('[');
                    sqlBuilder.append(i);
                    sqlBuilder.append("].");
                    sqlBuilder.append(keyProperty);
                    sqlBuilder.append('}');
                }
                sqlBuilder.append(')');
            }
        }
        sqlBuilder.append(')');

        appendLogicalDeletionWhereClause(sqlBuilder, entityInformation, false);

        return sqlBuilder;
    }

    protected static StringBuilder buildWhereClause(EntityInformation entityInformation, Method method, Object parameterObject) {
        return buildWhereClause(entityInformation, new PartTree(method.getName(), entityInformation.getEntityClass()),
                parameterObject);
    }

    protected static StringBuilder buildWhereClause(EntityInformation entityInformation, Iterable<OrPart> partTree,
            Object parameterObject) {

        StringBuilder sqlBuilder = new StringBuilder(128);
        boolean first = true;
        int index = 1;
        for (OrPart orPart : partTree) {
            boolean appendSuffix = !first;
            if (first) {
                first = false;
                sqlBuilder.append(" WHERE ");
            } else {
                sqlBuilder.append(" OR (");
            }
            boolean firstInternal = true;
            for (Part part : orPart) {
                if (firstInternal) {
                    firstInternal = false;
                } else {
                    sqlBuilder.append(" AND ");
                }
                index = processPart(sqlBuilder, entityInformation, part, parameterObject, index);
            }
            if (appendSuffix) {
                sqlBuilder.append(')');
            }
        }

        appendLogicalDeletionWhereClause(sqlBuilder, entityInformation, IterableUtils.isEmpty(partTree));

        return sqlBuilder;
    }

    private static int processPart(StringBuilder sqlBuilder, EntityInformation entityInformation, Part part,
            Object parameterObject, int index) {

        String property = part.getProperty().getSegment();
        String column = entityInformation.getTableName() + '.' + entityInformation.getCorrespondingColumn(property);
        PartTypeProcessor processor = PartTypeProcessor.getPartTypeProcessor(part.getType());
        return processor.processs(sqlBuilder, column, index, parameterObject, part.shouldIgnoreCase());
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, String paramName) {
        appendWhereClause(sqlBuilder, column, '=', paramName, null);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, int index) {
        appendWhereClause(sqlBuilder, column, operator, index, (IgnoreCaseType) null);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, String paramName) {
        appendWhereClause(sqlBuilder, column, operator, paramName, null);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, int index,
            IgnoreCaseType ignoreCaseType) {
        appendWhereClause(sqlBuilder, column, operator, index, null, ignoreCaseType);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, String paramName,
            IgnoreCaseType ignoreCaseType) {
        appendWhereClause(sqlBuilder, column, operator, paramName, null, ignoreCaseType);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, int index, String suffix,
            IgnoreCaseType ignoreCaseType) {
        appendWhereClause(sqlBuilder, column, operator, GENERAL_PARAM_NAME_PREFIX + index, suffix, ignoreCaseType);
    }

    static void appendWhereClause(StringBuilder sqlBuilder, String column, Object operator, String paramName, String suffix,
            IgnoreCaseType ignoreCaseType) {
        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append("UPPER(");
        }
        sqlBuilder.append(column);
        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append(')');
        }
        sqlBuilder.append(' ');
        sqlBuilder.append(operator);
        sqlBuilder.append(' ');
        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append("UPPER(");
        }
        sqlBuilder.append("#{");
        sqlBuilder.append(paramName);
        sqlBuilder.append('}');
        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append(')');
        }
        if (StringUtils.isNotEmpty(suffix)) {
            sqlBuilder.append(suffix);
        }
    }
}
