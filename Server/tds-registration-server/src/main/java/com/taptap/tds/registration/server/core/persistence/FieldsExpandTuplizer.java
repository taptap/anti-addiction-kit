package com.taptap.tds.registration.server.core.persistence;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FieldsExpandTuplizer {

    private final EntityInformation entityInformation;

    private final String selectListExpression;

    private final OneToOneExpandMetadata oneToOneExpandMetadata;

    private final Collection<OneToManyExpandMetadata> oneToManyExpandMetadatas;

    private boolean additionalIdRequired;

    public FieldsExpandTuplizer(Class<?> entityClass, FieldsExpand fieldsExpand, Set<String> excludeOneToOneEntities) {

        Objects.requireNonNull(entityClass);

        this.entityInformation = EntityInformationFactory.getEntityInformation(entityClass);
        this.oneToOneExpandMetadata = parseOneToOneExpand(fieldsExpand.getExpands(), excludeOneToOneEntities);
        this.oneToManyExpandMetadatas = parseOneToManyExpand(fieldsExpand.getExpands());
        this.selectListExpression = processFields(fieldsExpand.getFields());
    }

    public OneToOneExpandMetadata parseOneToOneExpand(Map<String, Set<String>> expands, Set<String> excludeOneToOneEntities) {

        if (Collections3.isEmpty(expands) || !entityInformation.hasOneToOneMetadata()) {
            return null;
        }

        Set<OneToOneMetadata> oneToOneMetadatas = new LinkedHashSet<>(4);
        BiMap<String, String> columnAliasMap = HashBiMap.create();
        expandLoop:
        for (Map.Entry<String, Set<String>> entry : expands.entrySet()) {

            StringBuilder columnPrefixBuilder = new StringBuilder(entityInformation.getTableNameAlias().length() + 16);
            EntityInformation previousEntityInformation = null;
            EntityInformation lastTargetEntityInformation = entityInformation;
            String[] targets = StringUtils.split(entry.getKey(), '.');
            for (String target : targets) {
                OneToOneMetadata oneToOneMetadata = lastTargetEntityInformation.getOneToOneMetadata(target);
                if (oneToOneMetadata == null) {
                    continue expandLoop;
                }
                if (!excludeOneToOneEntities.contains(target)) {
                    oneToOneMetadatas.add(oneToOneMetadata);
                }
                EntityInformation targetEntityInformation = EntityInformationFactory
                        .getEntityInformation(oneToOneMetadata.getTargetEntityClass());

                previousEntityInformation = lastTargetEntityInformation;
                lastTargetEntityInformation = targetEntityInformation;

                columnPrefixBuilder.append(previousEntityInformation.getTableNameAlias()).append('_')
                        .append(lastTargetEntityInformation.getTableNameAlias()).append('_');
            }
            if (Collections3.isEmpty(entry.getValue())) {
                for (String column : lastTargetEntityInformation.getColumns()) {
                    String columnAlias = columnPrefixBuilder + column;
                    columnAliasMap.put(columnAlias, lastTargetEntityInformation.getTableName() + "." + column);
                }
            } else {
                for (String propertyNameOrEntityName : entry.getValue()) {
                    OneToOneMetadata lastOneToOneMetadata = lastTargetEntityInformation
                            .getOneToOneMetadata(propertyNameOrEntityName);
                    // indicates the property is entity name, not column name
                    if (lastOneToOneMetadata != null) {
                        String entityName = propertyNameOrEntityName;
                        if (!excludeOneToOneEntities.contains(entityName)) {
                            oneToOneMetadatas.add(lastOneToOneMetadata);
                        }
                        EntityInformation targetEntityInformation = EntityInformationFactory
                                .getEntityInformation(lastOneToOneMetadata.getTargetEntityClass());

                        previousEntityInformation = lastTargetEntityInformation;
                        lastTargetEntityInformation = targetEntityInformation;

                        String columnPrefix = columnPrefixBuilder.append(previousEntityInformation.getTableNameAlias())
                                .append('_').append(lastTargetEntityInformation.getTableNameAlias()).append('_').toString();
                        for (String column : lastTargetEntityInformation.getColumns()) {
                            String columnAlias = columnPrefix + column;
                            columnAliasMap.put(columnAlias, lastTargetEntityInformation.getTableName() + "." + column);
                        }
                    } else {
                        String propertyName = propertyNameOrEntityName;
                        String column = lastTargetEntityInformation.getCorrespondingColumn(propertyName);
                        String columnAlias = columnPrefixBuilder + column;
                        columnAliasMap.put(columnAlias, lastTargetEntityInformation.getTableName() + "." + column);
                    }
                }
            }
        }

        if (Collections3.isEmpty(columnAliasMap)) {
            return null;
        }

        String selectListExpression = columnAliasMap.inverse().entrySet().stream().map(e -> e.getKey() + ' ' + e.getValue())
                .collect(Collectors.joining(","));
        return new OneToOneExpandMetadata(selectListExpression, oneToOneMetadatas);
    }

    private Collection<OneToManyExpandMetadata> parseOneToManyExpand(Map<String, Set<String>> expands) {

        if (Collections3.isEmpty(expands) || !entityInformation.hasOneToManyMetadata()) {
            return Collections.emptyList();
        }

        Map<String, OneToManyExpandMetadata> entityOneToManyExpandMetadataMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : expands.entrySet()) {
            String entityName = entry.getKey();
            OneToManyMetadata metadata = entityInformation.getOneToManyMetadata(entityName);
            if (metadata == null) {
                continue;
            }
            OneToManyExpandMetadata expandMetadata = new OneToManyExpandMetadata();
            expandMetadata.setOneToManyMetadata(metadata);
            entityOneToManyExpandMetadataMap.put(entityName, expandMetadata);
            EntityInformation targetEntityInformation = EntityInformationFactory
                    .getEntityInformation(metadata.getTargetEntityClass());
            if (Collections3.isNotEmpty(entry.getValue())) {
                for (String property : entry.getValue()) {
                    String column = targetEntityInformation.getCorrespondingColumn(property);
                    expandMetadata.addColumn(column);
                }
            } else {
                expandMetadata.setColumns(targetEntityInformation.getColumns());
            }
        }

        return entityOneToManyExpandMetadataMap.values();
    }

    private String processFields(Set<String> fields) {

        StringBuilder selectListExpressionBuilder = new StringBuilder(64);
        if (Collections3.isEmpty(fields)) {
            selectListExpressionBuilder.append(entityInformation.getSelectListExpression());
        } else {
            Identifier identifier = null;
            if (Collections3.isNotEmpty(oneToManyExpandMetadatas)) {
                identifier = entityInformation.getPrimaryIdentifier();
                this.additionalIdRequired = fields.add(identifier.getName());
            }

            // Non-empty one-to-one exclusion indicates there must be join statement that might be hit ambiguous column name exception
            boolean first = true;
            for (String field : fields) {
                String columnName = entityInformation.getCorrespondingColumn(field.trim());
                if (first) {
                    first = false;
                } else {
                    selectListExpressionBuilder.append(',');
                }
                selectListExpressionBuilder.append(entityInformation.getTableName());
                selectListExpressionBuilder.append('.');
                selectListExpressionBuilder.append(columnName);
            }
            if (identifier != null && isAdditionalIdRequired()) {
                fields.remove(identifier.getName());
            }
        }
        if (oneToOneExpandMetadata != null) {
            selectListExpressionBuilder.append(',');
            selectListExpressionBuilder.append(oneToOneExpandMetadata.getSelectListExpression());
        }
        return selectListExpressionBuilder.toString();
    }

    public EntityInformation getEntityInformation() {
        return entityInformation;
    }

    public String getSelectListExpression() {
        return selectListExpression;
    }

    public OneToOneExpandMetadata getOneToOneExpandMetadata() {
        return oneToOneExpandMetadata;
    }

    public Collection<OneToManyExpandMetadata> getOneToManyExpandMetadatas() {
        return oneToManyExpandMetadatas;
    }

    public boolean isAdditionalIdRequired() {
        return additionalIdRequired;
    }
}
