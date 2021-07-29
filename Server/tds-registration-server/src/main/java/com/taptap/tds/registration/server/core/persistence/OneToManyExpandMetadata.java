package com.taptap.tds.registration.server.core.persistence;

import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

public class OneToManyExpandMetadata {

    private Set<String> columns;

    private OneToManyMetadata oneToManyMetadata;

    public Set<String> getColumns() {
        return columns;
    }

    public void setColumns(Set<String> columns) {
        this.columns = new LinkedHashSet<>(columns);
    }

    public boolean addColumn(String column) {
        if (columns == null) {
            columns = new LinkedHashSet<>(8);
        }
        return columns.add(column);
    }

    public OneToManyMetadata getOneToManyMetadata() {
        return oneToManyMetadata;
    }

    public void setOneToManyMetadata(OneToManyMetadata oneToManyMetadata) {
        this.oneToManyMetadata = oneToManyMetadata;
    }

    public void processExpandRelation(Configuration configuration, Executor executor, Collection<?> entities)
            throws SQLException {

        String selectListExpression = null;
        OneToManyMetadata oneToManyMetadata = getOneToManyMetadata();
        Set<String> columns = getColumns();
        boolean removeForeignKey = false;
        String foreignKeyColumn = oneToManyMetadata.getForeignKeyColumn();
        if (columns != null) {
            removeForeignKey = columns.add(foreignKeyColumn);
            selectListExpression = StringUtils.join(columns, ",");
        }

        EntityInformation entityInformation = EntityInformationFactory.getEntityInformation(entities.iterator().next());
        Field field = oneToManyMetadata.getProperty();
        final Identifier identifier = entityInformation.getPrimaryIdentifier();
        List<Object> ids = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            ids.add(ReflectionUtils.invokeMethod(identifier.getGetterMethod(), entity));
        }

        OneToManyProviderMetadata provider = oneToManyMetadata.getProvider();
        MappedStatement mappedStatement = configuration.getMappedStatement(provider.getMappedStatementId());
        ParamNameResolver paramNameResolver = new ParamNameResolver(configuration, provider.getMethod());

        @SuppressWarnings("unchecked")
        ParamMap<Object> parameter = (ParamMap<Object>) paramNameResolver.getNamedParams(new Object[] { ids, null });
        parameter.put("selectListExpression", selectListExpression);
        parameter.put("joinExpression", null);

        List<Object> allTargetEntities = executor.query(mappedStatement, parameter, RowBounds.DEFAULT, null);
        if (Collections3.isEmpty(allTargetEntities)) {
            return;
        }
        PropertyDescriptor foreignKeyPropertyDescriptor = BeanUtils
                .getPropertyDescriptor(oneToManyMetadata.getTargetEntityClass(), oneToManyMetadata.getForeignKeyProperty());
        Method readMethod = foreignKeyPropertyDescriptor.getReadMethod();
        Map<Object, List<Object>> targetEntitiesMap = new HashMap<>();
        for (Object targetEntity : allTargetEntities) {
            Object foreignKey = ReflectionUtils.invokeMethod(readMethod, targetEntity);
            List<Object> targetEntities = targetEntitiesMap.computeIfAbsent(foreignKey, key -> new LinkedList<>());
            targetEntities.add(targetEntity);
        }

        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(field.getDeclaringClass(), field.getName());
        final Object[] args = { null };
        Method writeMethod = propertyDescriptor.getWriteMethod();
        Method foreignKeyWriteMethod = foreignKeyPropertyDescriptor.getWriteMethod();
        for (Object entity : entities) {
            Object id = ReflectionUtils.invokeMethod(identifier.getGetterMethod(), entity);
            List<Object> targetEntities = targetEntitiesMap.get(id);
            if (removeForeignKey && Collections3.isNotEmpty(targetEntities)) {
                for (Object targetEntity : targetEntities) {
                    ReflectionUtils.invokeMethod(foreignKeyWriteMethod, targetEntity, args);
                }
            }
            ReflectionUtils.invokeMethod(writeMethod, entity, targetEntities);
        }
    }
}
