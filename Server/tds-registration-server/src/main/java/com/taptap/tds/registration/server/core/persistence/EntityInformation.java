package com.taptap.tds.registration.server.core.persistence;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.taptap.tds.registration.server.core.domain.CreateTimeAwareEntity;
import com.taptap.tds.registration.server.core.domain.UpdateTimeAwareEntity;
import com.taptap.tds.registration.server.util.Collections3;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;


public class EntityInformation {

    private final Class<?> entityClass;

    private final String entityName;

    private final String tableNameAlias;

    private final String tableName;

    private final boolean createdAtPresent;

    private final boolean updatedAtPresent;

    private IdMetadata idMetadata;

    private String versionProperty;

    private Map<String, String> idColumnPropertyMap = Collections.emptyMap();

    private Map<String, Field> columnPropertyMap;

    private Map<String, String> propertyColumnMap;

    private Map<String, String> insertableColumns;

    private Map<String, String> updatableColumns;

    private String selectListExpression;

    private Supplier<Map<String, OneToOneMetadata>> targetEntityOneToOneMapSupplier;

    private volatile Map<String, OneToOneMetadata> targetEntityOneToOneMap;

    private Supplier<Map<String, OneToManyMetadata>> targetEntityOneToManyMapSupplier;

    private volatile Map<String, OneToManyMetadata> targetEntityOneToManyMap;

    private List<DuplicateKeyUpdateColumnMetadata> duplicateKeyUpdateColumns;

    private String logicalDeletionColumn;

    public EntityInformation(Class<?> entityClass, String entityName, String tableName) {
        this.entityClass = entityClass;
        this.entityName = entityName;
        this.tableNameAlias = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityName);
        this.tableName = tableName;
        this.createdAtPresent = CreateTimeAwareEntity.class.isAssignableFrom(entityClass);
        this.updatedAtPresent = UpdateTimeAwareEntity.class.isAssignableFrom(entityClass);
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getTableNameAlias() {
        return tableNameAlias;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCreatedAtPresent() {
        return createdAtPresent;
    }

    public boolean isUpdatedAtPresent() {
        return updatedAtPresent;
    }

    public IdMetadata getIdMetadata() {
        return idMetadata;
    }

    public void setIdMetadata(IdMetadata idMetadata) {
        this.idMetadata = idMetadata;
    }

    public Identifier getPrimaryIdentifier() {
        Set<Identifier> identifiers = idMetadata.getAttributes();
        if (identifiers.size() > 1) {
            throw new IllegalStateException(
                    "Can not get unique identifier due to there are composite ids in entity " + entityClass);
        }
        return identifiers.iterator().next();
    }

    public Set<String> getIdProperties() {
        return Collections3.transformToSet(idMetadata.getAttributes(), Identifier::getName);
    }

    public Set<String> getIdColumns() {
        return Collections3.transformToSet(getIdProperties(), this::getCorrespondingColumn);
    }

    public String getVersionProperty() {
        return versionProperty;
    }

    public void setVersionProperty(String versionProperty) {
        this.versionProperty = versionProperty;
    }

    public boolean isVersioning() {
        return versionProperty != null;
    }

    public Map<String, String> getIdColumnPropertyMap() {
        return idColumnPropertyMap;
    }

    public void setIdColumnPropertyMap(Map<String, String> idColumnPropertyMap) {
        this.idColumnPropertyMap = idColumnPropertyMap;
    }

    public Set<String> getColumns() {
        return columnPropertyMap.keySet();
    }

    public Map<String, Field> getColumnPropertyMap() {
        return columnPropertyMap;
    }

    public String getCorrespondingProperty(String column) {
        Field property = columnPropertyMap.get(column);
        if (property == null) {
            throw new IllegalArgumentException("wrong column: " + column);
        }
        return property.getName();
    }

    public boolean isValidProperty(String property) {
        return propertyColumnMap.containsKey(property);
    }

    public String getCorrespondingColumn(String property) {
        String column = propertyColumnMap.get(property);
        if (column == null) {
            throw new IllegalArgumentException("wrong property: " + property);
        }
        return column;
    }

    public void setColumnPropertyMap(Map<String, Field> columnPropertyMap) {
        this.columnPropertyMap = columnPropertyMap;
        Map<String, String> propertyColumnMap = Maps.newHashMapWithExpectedSize(columnPropertyMap.size());
        StringBuilder selectListExpressionBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Field> entry : columnPropertyMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                selectListExpressionBuilder.append(',');
            }
            selectListExpressionBuilder.append(tableName);
            selectListExpressionBuilder.append('.');
            selectListExpressionBuilder.append(entry.getKey());
            propertyColumnMap.put(entry.getValue().getName(), entry.getKey());
        }
        this.propertyColumnMap = propertyColumnMap;
        this.selectListExpression = selectListExpressionBuilder.toString();
    }

    public Map<String, String> getInsertableColumns() {
        return insertableColumns;
    }

    public void setInsertableColumns(Map<String, String> insertableColumns) {
        this.insertableColumns = insertableColumns;
    }

    public Map<String, String> getUpdatableColumns() {
        return updatableColumns;
    }

    public void setUpdatableColumns(Map<String, String> updatableColumns) {
        this.updatableColumns = updatableColumns;
    }

    public String getSelectListExpression() {
        return selectListExpression;
    }

    public void setTargetEntityOneToOneMapSupplier(Supplier<Map<String, OneToOneMetadata>> targetEntityOneToOneMapSupplier) {
        this.targetEntityOneToOneMapSupplier = targetEntityOneToOneMapSupplier;
    }

    public Map<String, OneToOneMetadata> getTargetEntityOneToOneMap() {
        if (targetEntityOneToOneMap == null) {
            if (targetEntityOneToOneMapSupplier == null) {
                throw new IllegalStateException("Both targetEntityOneToOneMap and targetEntityOneToOneMapSupplier are null");
            }
            this.targetEntityOneToOneMap = targetEntityOneToOneMapSupplier.get();
        }
        return targetEntityOneToOneMap;
    }

    public OneToOneMetadata getOneToOneMetadata(String targetEntityName) {
        return getTargetEntityOneToOneMap().get(targetEntityName);
    }

    public Collection<OneToOneMetadata> getOneToOneMetadatas() {
        return getTargetEntityOneToOneMap().values();
    }

    public boolean hasOneToOneMetadata() {
        return !getTargetEntityOneToOneMap().isEmpty();
    }

    public void setTargetEntityOneToManyMapSupplier(Supplier<Map<String, OneToManyMetadata>> targetEntityOneToManyMapSupplier) {
        this.targetEntityOneToManyMapSupplier = targetEntityOneToManyMapSupplier;
    }

    public Map<String, OneToManyMetadata> getTargetEntityOneToManyMap() {
        if (targetEntityOneToManyMap == null) {
            if (targetEntityOneToManyMapSupplier == null) {
                throw new IllegalStateException("Both targetEntityOneToManyMap and targetEntityOneToManyMapSupplier are null");
            }
            this.targetEntityOneToManyMap = targetEntityOneToManyMapSupplier.get();
        }
        return targetEntityOneToManyMap;
    }

    public OneToManyMetadata getOneToManyMetadata(String targetEntityName) {
        return getTargetEntityOneToManyMap().get(targetEntityName);
    }

    public boolean hasOneToManyMetadata() {
        return !getTargetEntityOneToManyMap().isEmpty();
    }

    public List<DuplicateKeyUpdateColumnMetadata> getDuplicateKeyUpdateColumns() {
        return duplicateKeyUpdateColumns;
    }

    public void setDuplicateKeyUpdateColumns(List<DuplicateKeyUpdateColumnMetadata> duplicateKeyUpdateColumns) {
        this.duplicateKeyUpdateColumns = duplicateKeyUpdateColumns;
    }

    public String getLogicalDeletionColumn() {
        return logicalDeletionColumn;
    }

    public void setLogicalDeletionColumn(String logicalDeletionColumn) {
        this.logicalDeletionColumn = logicalDeletionColumn;
    }
}
