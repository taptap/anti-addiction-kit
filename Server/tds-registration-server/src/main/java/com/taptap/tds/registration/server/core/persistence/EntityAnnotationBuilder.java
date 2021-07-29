package com.taptap.tds.registration.server.core.persistence;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.domain.LogicalDeletion;
import com.taptap.tds.registration.server.core.mysql.DuplicateKeyUpdate;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import javax.persistence.*;
import javax.persistence.criteria.JoinType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

public class EntityAnnotationBuilder {

    private Class<?> entityClass;

    private String entityName;

    public EntityAnnotationBuilder(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.entityName = parseEntityName(entityClass);
    }

    public EntityInformation parse() {

        String tableName = parseTableName();
        EntityInformation information = new EntityInformation(entityClass, entityName, tableName);
        parseColumns(information);
        information.setTargetEntityOneToOneMapSupplier(this::parseOneToOneAnnotation);
        information.setTargetEntityOneToManyMapSupplier(this::parseOneToManyAnnotation);
        Field[] versionProperties = FieldUtils.getFieldsWithAnnotation(entityClass, Version.class);
        if (ArrayUtils.isNotEmpty(versionProperties)) {
            if (versionProperties.length > 1) {
                throw new IllegalStateException("Multiple @Version annotated properties are found on type " + entityClass);
            }
            information.setVersionProperty(versionProperties[0].getName());
        }
        LogicalDeletion logicalDeletion = entityClass.getAnnotation(LogicalDeletion.class);
        if (logicalDeletion != null) {
            information.setLogicalDeletionColumn(logicalDeletion.value());
        }

        return information;
    }

    private String parseTableName() {

        String tableName = null;
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            tableName = table.name();
        }
        if (StringUtils.isEmpty(tableName)) {
            tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName());
        }

        return tableName;
    }

    private void parseColumns(final EntityInformation information) {

        final IdMetadata idMetadata = new IdMetadata();
        final Map<String, String> keyColumnPropertyMap = Maps.newHashMapWithExpectedSize(2);
        final Map<String, Field> columnPropertyMap = new HashMap<>();
        final LinkedHashMap<String, String> insertableColumns = new LinkedHashMap<>();
        final LinkedHashMap<String, String> updatableColumns = new LinkedHashMap<>();
        final List<DuplicateKeyUpdateColumnMetadata> duplicateKeyUpdateColumns = new ArrayList<>();
        ReflectionUtils.doWithFields(entityClass, field -> {

            PropertyDescriptor propertyDescriptor;
            try {
                propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
            } catch (IntrospectionException e) {
                throw new IllegalArgumentException(e);
            }
            Method readMethod = propertyDescriptor.getReadMethod();
            String columnName = null;
            boolean insertable = true;
            boolean updatable = true;
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                column = readMethod.getAnnotation(Column.class);
            }
            if (column != null) {
                columnName = column.name();
                insertable = column.insertable();
                updatable = column.updatable();
            }
            if (StringUtils.isEmpty(columnName)) {
                columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
            }
            columnPropertyMap.put(columnName, field);

            if (field.isAnnotationPresent(Id.class) || readMethod.isAnnotationPresent(Id.class) || "id".equals(field.getName())) {
                Identifier identifier = new Identifier(field.getName(), field.getDeclaringClass(),
                        propertyDescriptor.getReadMethod(), propertyDescriptor.getWriteMethod());
                idMetadata.addAttribute(identifier);
                keyColumnPropertyMap.put(columnName, field.getName());

                updatable = false;
                if (field.isAnnotationPresent(GeneratedValue.class) || readMethod.isAnnotationPresent(GeneratedValue.class)) {
                    idMetadata.setAutoGenerated(true);
                    insertable = false;
                }
            }

            if (insertable) {
                insertableColumns.put(columnName, field.getName());
            }
            if (updatable) {
                updatableColumns.put(columnName, field.getName());
            }

            DuplicateKeyUpdate duplicateKeyUpdate = field.getAnnotation(DuplicateKeyUpdate.class);
            if (duplicateKeyUpdate != null) {
                duplicateKeyUpdateColumns.add(new DuplicateKeyUpdateColumnMetadata(columnName, duplicateKeyUpdate.nullable()));
            }
        }, field -> !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Transient.class)
                && !field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(OneToMany.class)
                && !field.isAnnotationPresent(ManyToOne.class));

        if (Collections3.isEmpty(idMetadata.getAttributes())) {
            throw new IllegalStateException("No id property was found of entity " + entityClass);
        }

        if (idMetadata.getAttributes().size() > 1) {
            IdClass idClass = AnnotationUtils.findAnnotation(entityClass, IdClass.class);
            if (idClass == null) {
                throw new IllegalStateException(
                        "@IdClass is required of entity [" + entityClass + "] as composite primary keys were found");
            }
            idMetadata.setIdType(idClass.value());
        } else {
            idMetadata.setIdType(idMetadata.getAttributes().iterator().next().getType());
        }

        information.setIdMetadata(idMetadata);
        information.setIdColumnPropertyMap(Collections.unmodifiableMap(keyColumnPropertyMap));
        information.setColumnPropertyMap(Collections.unmodifiableMap(columnPropertyMap));
        information.setInsertableColumns(Collections.unmodifiableMap(insertableColumns));
        information.setUpdatableColumns(Collections.unmodifiableMap(updatableColumns));
        information.setDuplicateKeyUpdateColumns(duplicateKeyUpdateColumns);
    }

    private Map<String, OneToOneMetadata> parseOneToOneAnnotation() {

        final Map<String, OneToOneMetadata> entityNameOneToOneMap = new HashMap<>(8);
        ReflectionUtils.doWithFields(entityClass, field -> {

            boolean optional = false;
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            if (oneToOne != null) {
                optional = oneToOne.optional();
            } else {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                optional = manyToOne.optional();
            }

            String foreignKeyColumn = null;
            String referencedColumn = null;
            if (oneToOne != null && StringUtils.isNotEmpty(oneToOne.mappedBy())) {
                EntityInformation targetEntityInformation = EntityInformationFactory.getEntityInformation(field.getType());
                OneToOneMetadata oneToOneMetadata = targetEntityInformation.getOneToOneMetadata(oneToOne.mappedBy());
                if (oneToOneMetadata == null) {
                    throw new IllegalArgumentException(
                            "Mappedby of @OneToOne on " + entityClass + " field " + field + " is invalid");
                }
                foreignKeyColumn = oneToOneMetadata.getReferencedColumn();
                referencedColumn = oneToOneMetadata.getForeignKeyColumn();
            } else {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                if (joinColumn != null) {
                    foreignKeyColumn = joinColumn.name();
                    referencedColumn = joinColumn.referencedColumnName();
                }
                if (StringUtils.isEmpty(foreignKeyColumn)) {
                    foreignKeyColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()) + "_id";
                }
                if (StringUtils.isEmpty(referencedColumn)) {
                    EntityInformation targetEntityInformation = EntityInformationFactory.getEntityInformation(field.getType());
                    Set<Identifier> identifiers = targetEntityInformation.getIdMetadata().getAttributes();
                    if (identifiers.size() > 1) {
                        throw new IllegalArgumentException(
                                "Referenced column is required while the number of primary keys is greater than one of @OneToOne or @ManyToOne target entity");
                    }
                    referencedColumn = targetEntityInformation.getCorrespondingColumn(identifiers.iterator().next().getName());
                }
            }

            JoinType joinType = optional ? JoinType.LEFT : JoinType.INNER;

            OneToOneMetadata metadata = new OneToOneMetadata(field, foreignKeyColumn, referencedColumn, joinType);
            entityNameOneToOneMap.put(field.getName(), metadata);
        }, field -> field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class));

        return Collections.unmodifiableMap(entityNameOneToOneMap);
    }

    private Map<String, OneToManyMetadata> parseOneToManyAnnotation() {

        Map<String, OneToManyMetadata> entityNameOneToManyMap = new HashMap<>(8);
        ReflectionUtils.doWithFields(entityClass, new OneToManyAnnotatedFieldCallback(entityNameOneToManyMap),
                field -> field.isAnnotationPresent(OneToMany.class));

        return Collections.unmodifiableMap(entityNameOneToManyMap);
    }

    private static String parseEntityName(Class<?> entityClass) {
        String name = null;
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity != null) {
            name = entity.name();
        }
        if (StringUtils.isEmpty(name)) {
            name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, entityClass.getSimpleName());
        }
        return name;
    }

    private class OneToManyAnnotatedFieldCallback implements FieldCallback {

        private final Map<String, OneToManyMetadata> entityNameOneToManyMap;

        public OneToManyAnnotatedFieldCallback(Map<String, OneToManyMetadata> entityNameOneToManyMap) {
            this.entityNameOneToManyMap = entityNameOneToManyMap;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            OneToManyProvider provider = field.getAnnotation(OneToManyProvider.class);
            OneToManyProviderMetadata oneToManyProviderMetadata = getOneToManyProviderMetadata(provider, field);

            Class<?> targetEntityClass = oneToMany.targetEntity();
            if (void.class.equals(targetEntityClass)) {
                Type genericType = field.getGenericType();
                if (!(genericType instanceof ParameterizedType)) {
                    throw new IllegalStateException("Can not determine target entity of @OneToMany annoataion on field " + field);
                }
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                targetEntityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }

            String foreignKeyColumn = null;
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            String mappedBy = oneToMany.mappedBy();
            if (StringUtils.isNotEmpty(mappedBy)) {
                Field mappedByProperty = ReflectionUtils.findField(targetEntityClass, mappedBy);
                if (mappedByProperty == null) {
                    throw new IllegalStateException("Can not find [" + mappedBy + "] property of class " + targetEntityClass);
                }
                ManyToOne manyToOne = mappedByProperty.getAnnotation(ManyToOne.class);
                if (manyToOne == null) {
                    throw new IllegalStateException("Can not find @ManyToOne on field " + mappedByProperty);
                }
                JoinColumn mappedByJoinColumn = mappedByProperty.getAnnotation(JoinColumn.class);
                if (mappedByJoinColumn != null && StringUtils.isNotEmpty(mappedByJoinColumn.name())) {
                    foreignKeyColumn = mappedByJoinColumn.name();
                }
                if (StringUtils.isEmpty(foreignKeyColumn)) {
                    foreignKeyColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, mappedByProperty.getName()) + "_id";
                }
            } else if (joinColumn != null) {
                foreignKeyColumn = joinColumn.name();
            }
            if (StringUtils.isEmpty(foreignKeyColumn)) {
                String foreignKeyProperty = entityName + "Id";
                Field foreignKeyField = ReflectionUtils.findField(targetEntityClass, foreignKeyProperty);
                if (foreignKeyField == null) {
                    throw new IllegalStateException("Can not determine foreign key of @OneToMany annoataion on field " + field);
                }
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    foreignKeyColumn = column.name();
                }
                if (StringUtils.isEmpty(foreignKeyColumn)) {
                    foreignKeyColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, foreignKeyProperty);
                }
            }

            OneToManyMetadata metadata = new OneToManyMetadata(field, targetEntityClass, foreignKeyColumn,
                    oneToManyProviderMetadata);
            entityNameOneToManyMap.put(field.getName(), metadata);
        }

        private OneToManyProviderMetadata getOneToManyProviderMetadata(OneToManyProvider provider, Field field) {
            if (provider == null) {
                throw new IllegalStateException(
                        "Can not find @OneToManyProvider on field " + field + " which is required if @OneToMany presents.");
            }
            Class<?> mapperInterface = null;
            if (provider.mapperInterface() != void.class) {
                mapperInterface = provider.mapperInterface();
            } else if (StringUtils.isNotEmpty(provider.mapperInterfaceName())) {
                try {
                    mapperInterface = ClassUtils.forName(provider.mapperInterfaceName(), null);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException(
                        "Can not find @OneToManyProvider on field " + field + " which is required if @OneToMany presents.");
            }

            Method method = ClassUtils.getMethod(mapperInterface, provider.method(), List.class, FieldsExpand.class);
            return new OneToManyProviderMetadata(mapperInterface.getCanonicalName() + "." + provider.method(), method);
        }
    }
}
