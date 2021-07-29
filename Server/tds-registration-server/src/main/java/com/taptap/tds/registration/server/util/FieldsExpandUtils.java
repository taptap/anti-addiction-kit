package com.taptap.tds.registration.server.util;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.Collection;

public abstract class FieldsExpandUtils {

    public static boolean hasExpands(FieldsExpand fieldsExpand) {
        return fieldsExpand == null ? false : Collections3.isNotEmpty(fieldsExpand.getExpands());
    }

    @SuppressWarnings("unchecked")
    public static <T> T copyEntityWithRequiredProperties(T entity, FieldsExpand fieldsExpand) {
        if (entity == null || fieldsExpand == null || Collections3.isEmpty(fieldsExpand.getFields())) {
            return entity;
        }
        ConfigurablePropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);
        BeanWrapper copiedEntityWrapper = new BeanWrapperImpl(entity.getClass());
        for (String propertyName : fieldsExpand.getFields()) {
            copiedEntityWrapper.setPropertyValue(propertyName, entityPropertyAccessor.getPropertyValue(propertyName));
        }
        return (T) copiedEntityWrapper.getWrappedInstance();
    }

    public static <T> void removeAdditionalFields(Collection<T> entities, FieldsExpand fieldsExpand) {
        if (Collections3.isEmpty(entities) || fieldsExpand == null || Collections3.isEmpty(fieldsExpand.getAdditionalFields())) {
            return;
        }
        for (T entity : entities) {
            removeAdditionalFields(entity, fieldsExpand);
        }
    }

    public static <T> void removeAdditionalFields(T entity, FieldsExpand fieldsExpand) {
        if (entity == null || fieldsExpand == null || Collections3.isEmpty(fieldsExpand.getAdditionalFields())) {
            return;
        }
        ConfigurablePropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(entity);
        for (String field : fieldsExpand.getAdditionalFields()) {
            entityPropertyAccessor.setPropertyValue(field, null);
        }
    }
}
