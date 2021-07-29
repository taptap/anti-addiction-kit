package com.taptap.tds.registration.server.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.*;


public class FieldsExpand implements Serializable {

    private static final long serialVersionUID = -627120118901131196L;

    private Set<String> fields;

    private Map<String, Set<String>> expands;

    @JsonIgnore
    private transient Boolean forUpdate;

    @JsonIgnore
    private transient Set<String> additionalFields;

    @JsonIgnore
    private transient String selectListExpression;

    @JsonIgnore
    private transient String joinExpression;

    protected FieldsExpand() {
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public void addFields(String... fields) {
        if (this.fields == null) {
            this.fields = new LinkedHashSet<>();
        }
        Collections.addAll(this.fields, fields);
    }

    public Set<String> getAdditionalFields() {
        return additionalFields;
    }

    public void addAdditionalFields(String... fields) {
        if (this.fields == null || ArrayUtils.isEmpty(fields)) {
            return;
        }
        if (this.additionalFields == null) {
            this.additionalFields = new HashSet<>(4);
        }
        for (String field : fields) {
            if (!this.fields.contains(field)) {
                additionalFields.add(field);
                addFields(field);
            }
        }
    }

    public Map<String, Set<String>> getExpands() {
        return expands;
    }

    public void setExpands(Map<String, Set<String>> expands) {
        this.expands = expands;
    }

    public FieldsExpand addExpand(String target, String... properties) {
        if (this.expands == null) {
            this.expands = new HashMap<>(4);
        }
        Set<String> existingProperties = expands.get(target);
        if (existingProperties == null) {
            existingProperties = new LinkedHashSet<>(4);
            expands.put(target, existingProperties);
        }
        if (ArrayUtils.isNotEmpty(properties)) {
            Collections.addAll(existingProperties, properties);
        }
        return this;
    }

    public void setExpand(String expand) {
        parseExpand(expand);
    }

    private void parseExpand(String expandExpression) {
        if (StringUtils.isBlank(expandExpression)) {
            return;
        }
        String[] expands = StringUtils.split(expandExpression, ",");
        for (String expand : expands) {
            int expandPropertySeparatorIndex = expand.lastIndexOf(".");
            if (expandPropertySeparatorIndex > 0) {
                addExpand(expand.substring(0, expandPropertySeparatorIndex), expand.substring(expandPropertySeparatorIndex + 1));
            } else {
                addExpand(expand);
            }
        }
    }

    public Boolean getForUpdate() {
        return forUpdate;
    }

    public FieldsExpand forUpdate(Boolean forUpdate) {
        this.forUpdate = forUpdate;
        return this;
    }

    public String buildExpandExpression() {
        if (Collections3.isEmpty(expands)) {
            return "";
        }
        StringBuilder expandBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Set<String>> entry : getExpands().entrySet()) {
            Set<String> properties = entry.getValue();
            if (first) {
                first = false;
            } else {
                expandBuilder.append(',');
            }
            if (Collections3.isEmpty(properties)) {
                expandBuilder.append(entry.getKey());
                continue;
            }
            first = true;
            for (String property : properties) {
                if (first) {
                    first = false;
                } else {
                    expandBuilder.append(',');
                }
                expandBuilder.append(entry.getKey()).append('.').append(property);
            }
        }
        return expandBuilder.toString();
    }

    public String getSelectListExpression() {
        return selectListExpression;
    }

    public void setSelectListExpression(String selectListExpression) {
        this.selectListExpression = selectListExpression;
    }

    public String getJoinExpression() {
        return joinExpression;
    }

    public void setJoinExpression(String joinExpression) {
        this.joinExpression = joinExpression;
    }

    public void reset() {
        setExpands(null);
        setFields(null);
        setSelectListExpression(null);
        setJoinExpression(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expands == null) ? 0 : expands.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
        FieldsExpand other = (FieldsExpand) obj;
        if (expands == null) {
            if (other.expands != null) {
                return false;
            }
        } else if (!expands.equals(other.expands)) {
            return false;
        }
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static FieldsExpand create() {
        return new FieldsExpand();
    }

    public static FieldsExpand createForUpdate() {
        FieldsExpand fieldsExpand = create();
        fieldsExpand.forUpdate = true;
        return fieldsExpand;
    }

    public static FieldsExpand createWithFields(String... fields) {
        FieldsExpand fieldsExpand = new FieldsExpand();
        if (ArrayUtils.isNotEmpty(fields)) {
            fieldsExpand.setFields(Sets.newHashSet(fields));
        }
        return fieldsExpand;
    }

    public static FieldsExpand createWithExpand(String target, String... properties) {
        FieldsExpand fieldsExpand = new FieldsExpand();
        fieldsExpand.addExpand(target, properties);
        return fieldsExpand;
    }
}
