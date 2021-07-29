package com.taptap.tds.registration.server.core.enums;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class ClassPathEnumValueScanner<T extends EnumValue<?>> extends ClassPathScanningCandidateComponentProvider {

    public ClassPathEnumValueScanner(Class<T> type) {
        super(false);
        addIncludeFilter(new AssignableTypeFilter(type));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isIndependent();
    }
}
