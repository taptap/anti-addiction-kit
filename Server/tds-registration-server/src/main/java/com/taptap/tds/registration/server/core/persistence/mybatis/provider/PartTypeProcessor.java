package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.persistence.mybatis.util.MybatisUtils;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum PartTypeProcessor {

    SIMPLE_PROPERTY {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, '=', index, ignoreCaseType);
            return index + 1;
        }
    },
    BETWEEN {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append("UPPER(");
            }
            sqlBuilder.append(column);
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append(')');
            }
            sqlBuilder.append(" BETWEEN ");
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append("UPPER(");
            }
            sqlBuilder.append("#{");
            sqlBuilder.append(SqlProviderSupport.GENERAL_PARAM_NAME_PREFIX);
            sqlBuilder.append(index++);
            sqlBuilder.append('}');
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append(')');
            }
            sqlBuilder.append(" AND ");
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append("UPPER(");
            }
            sqlBuilder.append("#{");
            sqlBuilder.append(SqlProviderSupport.GENERAL_PARAM_NAME_PREFIX);
            sqlBuilder.append(index++);
            sqlBuilder.append('}');
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append(')');
            }
            return index;
        }
    },
    IS_NOT_NULL {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            sqlBuilder.append(column);
            sqlBuilder.append(" IS NOT NULL");
            return index;
        }
    },
    IS_NULL {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            sqlBuilder.append(column);
            sqlBuilder.append(" IS NULL");
            return index;
        }
    },
    LESS_THAN {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, '<', index, ignoreCaseType);
            return index + 1;
        }
    },
    LESS_THAN_EQUAL {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "<=", index, ignoreCaseType);
            return index + 1;
        }
    },
    GREATER_THAN {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, '>', index, ignoreCaseType);
            return index + 1;
        }
    },
    GREATER_THAN_EQUAL {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, ">=", index, ignoreCaseType);
            return index + 1;
        }
    },
    BEFORE {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, '<', index, ignoreCaseType);
            return index + 1;
        }
    },
    AFTER {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, '>', index, ignoreCaseType);
            return index + 1;
        }
    },
    NOT_LIKE {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "NOT LIKE", index, ignoreCaseType);
            return index + 1;
        }
    },
    LIKE {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "LIKE", index, ignoreCaseType);
            return index + 1;
        }
    },
    STARTING_WITH {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "LIKE CONCAT(", index, ", '%')", ignoreCaseType);
            return index + 1;
        }
    },
    ENDING_WITH {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "LIKE CONCAT('%',", index, ")", ignoreCaseType);
            return index + 1;
        }
    },
    NOT_CONTAINING {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "NOT LIKE CONCAT('%', ", index, ", '%')", ignoreCaseType);
            return index + 1;
        }
    },
    CONTAINING {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "LIKE CONCAT('%', ", index, ", '%')", ignoreCaseType);
            return index + 1;
        }
    },
    NOT_IN {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            processInNotIn(sqlBuilder, column, index, parameterObject, ignoreCaseType, true);
            return index + 1;
        }
    },
    IN {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            processInNotIn(sqlBuilder, column, index, parameterObject, ignoreCaseType, false);
            return index + 1;
        }
    },
    TRUE {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            sqlBuilder.append(column);
            sqlBuilder.append(" = true");
            return index;
        }
    },
    FALSE {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            sqlBuilder.append(column);
            sqlBuilder.append(" = false");
            return index;
        }
    },
    NEGATING_SIMPLE_PROPERTY {
        @Override
        public int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
                IgnoreCaseType ignoreCaseType) {
            SqlProviderSupport.appendWhereClause(sqlBuilder, column, "<>", index, ignoreCaseType);
            return index + 1;
        }
    };

    private static void processInNotIn(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
            IgnoreCaseType ignoreCaseType, boolean notIn) {

        String paramName = SqlProviderSupport.GENERAL_PARAM_NAME_PREFIX + index;
        int size = getCollectionSize(parameterObject, paramName);

        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append("UPPER(");
        }
        sqlBuilder.append(column);
        if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
            sqlBuilder.append(')');
        }
        if (notIn) {
            sqlBuilder.append(" NOT");
        }
        sqlBuilder.append(" IN (");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sqlBuilder.append(',');
            }
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append("UPPER(");
            }
            sqlBuilder.append("#{");
            sqlBuilder.append(paramName);
            sqlBuilder.append('[');
            sqlBuilder.append(i);
            sqlBuilder.append("]}");
            if (ignoreCaseType == IgnoreCaseType.ALWAYS) {
                sqlBuilder.append(')');
            }
        }
        sqlBuilder.append(')');
    }

    private static int getCollectionSize(Object parameterObject, String paramName) {

        Object collection = parameterObject;
        if (parameterObject instanceof Map) {
            collection = MybatisUtils.extractMapperMethodParameter(parameterObject, paramName);
        }
        Objects.requireNonNull(collection, "The '" + paramName + "' parameter is null.");

        if (collection instanceof List) {
            return ((List<?>) collection).size();
        } else if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        } else {
            throw new IllegalArgumentException("The '" + paramName + "' parameter of " + collection + " is not a List or Array.");
        }
    }

    public abstract int processs(StringBuilder sqlBuilder, String column, int index, Object parameterObject,
            IgnoreCaseType ignoreCaseType);

    public static PartTypeProcessor getPartTypeProcessor(Part.Type partType) {
        return Enum.valueOf(PartTypeProcessor.class, partType.name());
    }
}
