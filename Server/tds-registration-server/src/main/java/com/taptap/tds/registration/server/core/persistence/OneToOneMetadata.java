package com.taptap.tds.registration.server.core.persistence;

import javax.persistence.criteria.JoinType;
import java.lang.reflect.Field;

public class OneToOneMetadata {

    private final Field property;

    private final String foreignKeyColumn;

    private final String referencedColumn;

    private final JoinType joinType;

    public OneToOneMetadata(Field property, String foreignKeyColumn, String referencedColumn, JoinType joinType) {
        this.property = property;
        this.foreignKeyColumn = foreignKeyColumn;
        this.referencedColumn = referencedColumn;
        this.joinType = joinType;
    }

    public Field getProperty() {
        return property;
    }

    public Class<?> getTargetEntityClass() {
        return property.getType();
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((foreignKeyColumn == null) ? 0 : foreignKeyColumn.hashCode());
        result = prime * result + ((referencedColumn == null) ? 0 : referencedColumn.hashCode());
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
        OneToOneMetadata other = (OneToOneMetadata) obj;
        if (property == null) {
            if (other.property != null) {
                return false;
            }
        } else if (!property.equals(other.property)) {
            return false;
        }
        if (foreignKeyColumn == null) {
            if (other.foreignKeyColumn != null) {
                return false;
            }
        } else if (!foreignKeyColumn.equals(other.foreignKeyColumn)) {
            return false;
        }
        if (referencedColumn == null) {
            if (other.referencedColumn != null) {
                return false;
            }
        } else if (!referencedColumn.equals(other.referencedColumn)) {
            return false;
        }
        return true;
    }
}
