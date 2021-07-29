package com.taptap.tds.registration.server.core.persistence;

import java.lang.reflect.Field;

public class OneToManyMetadata {

    private final Field property;

    private final Class<?> targetEntityClass;

    private final String foreignKeyColumn;

    private String foreignKeyProperty;

    private final OneToManyProviderMetadata provider;

    public OneToManyMetadata(Field property, Class<?> targetEntityClass, String foreignKeyColumn,
           OneToManyProviderMetadata provider) {
        this.property = property;
        this.targetEntityClass = targetEntityClass;
        this.foreignKeyColumn = foreignKeyColumn;
        this.provider = provider;
    }

    public Field getProperty() {
        return property;
    }

    public Class<?> getTargetEntityClass() {
        return targetEntityClass;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public String getForeignKeyProperty() {
        if (foreignKeyProperty == null) {
            EntityInformation targetEntityInformation = EntityInformationFactory.getEntityInformation(targetEntityClass);
            foreignKeyProperty = targetEntityInformation.getCorrespondingProperty(foreignKeyColumn);
        }
        return foreignKeyProperty;
    }

    public OneToManyProviderMetadata getProvider() {
        return provider;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((foreignKeyColumn == null) ? 0 : foreignKeyColumn.hashCode());
        result = prime * result + ((targetEntityClass == null) ? 0 : targetEntityClass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OneToManyMetadata other = (OneToManyMetadata) obj;
        if (foreignKeyColumn == null) {
            if (other.foreignKeyColumn != null) {
                return false;
            }
        } else if (!foreignKeyColumn.equals(other.foreignKeyColumn)) {
            return false;
        }
        if (targetEntityClass == null) {
            if (other.targetEntityClass != null) {
                return false;
            }
        } else if (!targetEntityClass.equals(other.targetEntityClass)) {
            return false;
        }
        return true;
    }
}
