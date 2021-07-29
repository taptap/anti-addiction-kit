package com.taptap.tds.registration.server.core.persistence.mybatis.plugin;


import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import com.taptap.tds.registration.server.core.persistence.EntityInformationFactory;
import com.taptap.tds.registration.server.core.persistence.OneToOneMetadata;
import com.taptap.tds.registration.server.core.persistence.mybatis.dialect.Dialect;
import com.taptap.tds.registration.server.core.persistence.mybatis.paging.SortableRowBounds;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class OffsetLimitInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;

    private static final int PARAMETER_INDEX = 1;

    private static final int ROW_BOUNDS_INDEX = 2;

    private static final int RESULT_HANDLER_INDEX = 3;

    private final Dialect dialect;

    private final Field additionalParametersField;

    public OffsetLimitInterceptor(Dialect dialect) {
        this.dialect = dialect;
        this.additionalParametersField = ReflectionUtils.findField(BoundSql.class, "additionalParameters");
        ReflectionUtils.makeAccessible(additionalParametersField);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        List<?> result = processIntercept(invocation);
        return result != null ? result : invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private List<?> processIntercept(Invocation invocation) throws Exception {

        Object[] args = invocation.getArgs();

        MappedStatement mappedStatement = (MappedStatement) args[MAPPED_STATEMENT_INDEX];
        Object parameter = args[PARAMETER_INDEX];
        RowBounds rowBounds = (RowBounds) args[ROW_BOUNDS_INDEX];
        int offset = rowBounds.getOffset();
        int limit = rowBounds.getLimit();
        if (limit <= 0) {
            return null;
        }
        if (!dialect.supportsLimit()) {
            return null;
        }
        if (offset == RowBounds.NO_ROW_OFFSET && limit == RowBounds.NO_ROW_LIMIT) {
            return null;
        }

        ResultHandler<?> resultHandler = (ResultHandler<?>) args[RESULT_HANDLER_INDEX];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = mappedStatement.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        String sql = boundSql.getSql().trim();

        String sortItemString = processSort(rowBounds, mappedStatement);
        if (sortItemString != null) {
            sql = sql + " ORDER BY " + sortItemString;
        }

        if (dialect.supportsLimitOffset()) {
            sql = dialect.getLimitString(sql, offset, limit);
        } else {
            sql = dialect.getLimitString(sql, 0, limit);
        }

        BoundSql pageBoundSql = new BoundSql(mappedStatement.getConfiguration(), sql, boundSql.getParameterMappings(), parameter);

        @SuppressWarnings("unchecked")
        Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
        // set dynamic parameters
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            pageBoundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }

        return executor.query(mappedStatement, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, pageBoundSql);
    }

    private String processSort(RowBounds rowBounds, MappedStatement mappedStatement) {

        if (!(rowBounds instanceof SortableRowBounds)) {
            return null;
        }

        SortableRowBounds sortableRowBounds = (SortableRowBounds) rowBounds;
        if (sortableRowBounds.getSort() == null) {
            return null;
        }

        Class<?> type = mappedStatement.getResultMaps().iterator().next().getType();
        EntityInformation entityInformation = EntityInformationFactory.getEntityInformation(type);
        StringBuilder sortBuilder = new StringBuilder();
        boolean first = true;
        for (Order order : sortableRowBounds.getSort()) {
            if (first) {
                first = false;
            } else {
                sortBuilder.append(',');
            }

            String tableName = null;
            String columnName = null;
            String sortItem = order.getProperty();
            int propertySeparatorIndex = sortItem.indexOf(".");
            if (propertySeparatorIndex > 0) {
                String targetEntity = sortItem.substring(0, propertySeparatorIndex);
                OneToOneMetadata oneToOneMetadata = entityInformation.getOneToOneMetadata(targetEntity);
                if (oneToOneMetadata == null) {
                    throw new IllegalArgumentException("Illegal sort item [" + sortItem + "]");
                }
                EntityInformation targetEntityInformation = EntityInformationFactory
                        .getEntityInformation(oneToOneMetadata.getTargetEntityClass());
                String propertyName = sortItem.substring(propertySeparatorIndex + 1);
                if (targetEntityInformation.isValidProperty(propertyName)) {
                    columnName = targetEntityInformation.getCorrespondingColumn(propertyName);
                    tableName = targetEntityInformation.getTableName();
                }
            } else {
                EntityInformation targetEntityInformation = determineSortItemEntity(entityInformation, sortItem);
                if (targetEntityInformation != null) {
                    String propertyName = sortItem;
                    columnName = targetEntityInformation.getCorrespondingColumn(propertyName);
                    tableName = targetEntityInformation.getTableName();
                }
            }
            if (StringUtils.isEmpty(columnName)) {
                throw new IllegalArgumentException("Illegal sort item [" + sortItem + "]");
            }

            sortBuilder.append(tableName);
            sortBuilder.append('.');
            sortBuilder.append(columnName);
            sortBuilder.append(' ');
            sortBuilder.append(order.getDirection());
        }

        return sortBuilder.toString();
    }

    private EntityInformation determineSortItemEntity(EntityInformation entityInformation, String sortItem) {
        if (entityInformation.isValidProperty(sortItem)) {
            return entityInformation;
        }

        Collection<OneToOneMetadata> oneToOneMetadatas = entityInformation.getOneToOneMetadatas();
        if (Collections3.isEmpty(oneToOneMetadatas)) {
            return null;
        }
        for (OneToOneMetadata oneToOneMetadata : oneToOneMetadatas) {
            EntityInformation targetEntityInformation = EntityInformationFactory
                    .getEntityInformation(oneToOneMetadata.getTargetEntityClass());
            if (targetEntityInformation.isValidProperty(sortItem)) {
                return targetEntityInformation;
            }
        }
        return null;
    }
}
