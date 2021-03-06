package com.taptap.tds.registration.server.core.persistence;

import java.util.HashSet;
import java.util.Set;

public class IdMetadata {

    private Set<Identifier> attributes;

    private Class<?> idType;

    private boolean autoGenerated = false;

    public Set<Identifier> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<Identifier> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Identifier identifier) {
        if (attributes == null) {
            attributes = new HashSet<>(4);
        }
        attributes.add(identifier);
    }

    public Class<?> getIdType() {
        return idType;
    }

    public void setIdType(Class<?> idType) {
        this.idType = idType;
    }

    public boolean isCompositePrimaryKeys() {
        return attributes.size() > 1;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }
}
