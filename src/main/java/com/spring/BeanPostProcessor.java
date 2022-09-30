package com.spring;

public interface BeanPostProcessor {
    Object postProcessorBeforeInitializing(Object bean, String beanName);
    Object postProcessorAfterInitializing(Object bean, String beanName);
}
