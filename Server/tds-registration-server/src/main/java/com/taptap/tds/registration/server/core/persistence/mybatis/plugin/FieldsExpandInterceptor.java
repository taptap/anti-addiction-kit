package com.taptap.tds.registration.server.core.persistence.mybatis.plugin;

import com.google.common.collect.ImmutableSet;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.*;
import com.taptap.tds.registration.server.core.persistence.mybatis.annotation.ExcludeOneToOne;
import com.taptap.tds.registration.server.core.persistence.mybatis.util.MybatisUtils;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class FieldsExpandInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;

    private static final int PARAMETER_INDEX = 1;

    @Override
    public Object intercept(Invocation invocation) throws Exception {

        FieldsExpandTuplizer tuplizer = processIntercept(invocation);
        Object result = invocation.proceed();
        if (result == null || tuplizer == null) {
            return result;
        }

        Collection<OneToManyExpandMetadata> oneToManyExpandMetadatas = tuplizer.getOneToManyExpandMetadatas();
        if (Collections3.isNotEmpty(oneToManyExpandMetadatas)) {
            Collection<?> entities = null;
            if (result instanceof Collection) {
                entities = (Collection<?>) result;
                if (entities.isEmpty()) {
                    return result;
                }
            } else {
                entities = Collections.singleton(result);
            }
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[MAPPED_STATEMENT_INDEX];
            Executor executor = (Executor) invocation.getTarget();
            for (OneToManyExpandMetadata oneToManyExpandMetadata : oneToManyExpandMetadatas) {
                oneToManyExpandMetadata.processExpandRelation(mappedStatement.getConfiguration(), executor, entities);
            }
            if (tuplizer.isAdditionalIdRequired()) {
                EntityInformation entityInformation = tuplizer.getEntityInformation();
                for (Object entity : entities) {
                    for (Identifier identifier : entityInformation.getIdMetadata().getAttributes()) {
                        Object[] args = { null };
                        ReflectionUtils.invokeMethod(identifier.getSetterMethod(), entity, args);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    @SuppressWarnings("unchecked")
    private FieldsExpandTuplizer processIntercept(Invocation invocation) {

        Object[] args = invocation.getArgs();

        MappedStatement mappedStatement = (MappedStatement) args[MAPPED_STATEMENT_INDEX];
        Object parameter = args[PARAMETER_INDEX];

        Class<?> entityClass = mappedStatement.getResultMaps().iterator().next().getType();
        if (BeanUtils.isSimpleValueType(entityClass)) {
            return null;
        }

        FieldsExpand fieldsExpand = null;
        FieldsExpandTuplizer tuplizer = null;
        if (parameter instanceof Map) {
            Map<String, Object> paramMap = (Map<String, Object>) parameter;
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getValue() instanceof FieldsExpand) {
                    fieldsExpand = (FieldsExpand) entry.getValue();
                    tuplizer = processFieldsExpand(fieldsExpand, mappedStatement);
                    paramMap.put("selectListExpression", fieldsExpand.getSelectListExpression());
                    paramMap.put("joinExpression", fieldsExpand.getJoinExpression());
                    paramMap.put("forUpdate", fieldsExpand.getForUpdate());
                    break;
                }
            }
        } else if (parameter instanceof FieldsExpand) {
            fieldsExpand = (FieldsExpand) parameter;
            tuplizer = processFieldsExpand(fieldsExpand, mappedStatement);
        }
        // no FieldsExpand found, check parameter type to handle null FieldsExpand case
        if (fieldsExpand == null) {
            Method method = MybatisUtils.getMappedStatementMethod(mappedStatement);
            for (Parameter methodParameter : method.getParameters()) {
                if (FieldsExpand.class.isAssignableFrom(methodParameter.getType())) {
                    EntityInformation entityInformation = EntityInformationFactory.getEntityInformation(entityClass);
                    if (parameter == null) {
                        fieldsExpand = FieldsExpand.create();
                        fieldsExpand.setSelectListExpression(entityInformation.getSelectListExpression());
                        args[PARAMETER_INDEX] = fieldsExpand;
                    } else if (parameter instanceof Map) {
                        Map<String, Object> param = (Map<String, Object>) parameter;
                        param.put(methodParameter.getName(), fieldsExpand);
                        param.put("selectListExpression", entityInformation.getSelectListExpression());
                        param.put("joinExpression", null);
                        param.put("forUpdate", null);
                    } else {
                        throw new IllegalStateException(
                                "Can not extract FieldsExpand from Mybatis parameter object, thus FieldsExpand must be either null or Map of method "
                                        + method);
                    }
                    break;
                }
            }
        }
        return tuplizer;
    }

    private FieldsExpandTuplizer processFieldsExpand(FieldsExpand fieldsExpand, MappedStatement mappedStatement) {
        Class<?> entityClass = mappedStatement.getResultMaps().iterator().next().getType();
        Method method = MybatisUtils.getMappedStatementMethod(mappedStatement);
        ExcludeOneToOne excludeOneToOne = method.getAnnotation(ExcludeOneToOne.class);
        Set<String> excludeOneToOneEntities = excludeOneToOne == null ? Collections.<String> emptySet()
                : ImmutableSet.copyOf(excludeOneToOne.value());
        FieldsExpandTuplizer tuplizer = new FieldsExpandTuplizer(entityClass, fieldsExpand, excludeOneToOneEntities);
        fieldsExpand.setSelectListExpression(tuplizer.getSelectListExpression());
        if (tuplizer.getOneToOneExpandMetadata() != null) {
            fieldsExpand.setJoinExpression(tuplizer.getOneToOneExpandMetadata().getJoinExpression());
        }
        return tuplizer;
    }
}
