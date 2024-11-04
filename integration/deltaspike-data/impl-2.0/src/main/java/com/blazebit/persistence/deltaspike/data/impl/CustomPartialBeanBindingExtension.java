/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl;

import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.handler.QueryHandler;
import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;
import org.apache.deltaspike.partialbean.impl.PartialBeanDescriptor;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CustomPartialBeanBindingExtension extends PartialBeanBindingExtension {

    private boolean isActivated() {
        try {
            Field descriptorsField = PartialBeanBindingExtension.class.getDeclaredField("isActivated");
            descriptorsField.setAccessible(true);
            return (boolean) descriptorsField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Class<? extends Annotation>, PartialBeanDescriptor> getDescriptors() {
        try {
            Field descriptorsField = PartialBeanBindingExtension.class.getDeclaredField("descriptors");
            descriptorsField.setAccessible(true);
            return (Map<Class<? extends Annotation>, PartialBeanDescriptor>) descriptorsField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private IllegalStateException getDefinitionError() {
        try {
            Field descriptorsField = PartialBeanBindingExtension.class.getDeclaredField("definitionError");
            descriptorsField.setAccessible(true);
            return (IllegalStateException) descriptorsField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDefinitionError(IllegalStateException definitionError) {
        try {
            Field descriptorsField = PartialBeanBindingExtension.class.getDeclaredField("definitionError");
            descriptorsField.setAccessible(true);
            descriptorsField.set(this, definitionError);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <X> void findInvocationHandlerBindings(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
        if (isActivated() && getDefinitionError() == null) {
            Map<Class<? extends Annotation>, PartialBeanDescriptor> descriptors = getDescriptors();
            @SuppressWarnings("unchecked")
            Class<? extends InvocationHandler> beanClass = (Class<? extends InvocationHandler>) pat.getAnnotatedType().getJavaClass();
            Class<? extends Annotation> bindingClass = this.extractBindingClass(pat);
            if (bindingClass != null) {
                if (bindingClass.equals(Repository.class) && beanClass.equals(QueryHandler.class)) {
                    return;
                }
                PartialBeanDescriptor descriptor;
                if (!beanClass.isInterface() && !Modifier.isAbstract(beanClass.getModifiers())) {
                    if (InvocationHandler.class.isAssignableFrom(beanClass)) {
                        descriptor = descriptors.get(bindingClass);
                        if (descriptor == null) {
                            descriptor = new PartialBeanDescriptor(bindingClass, beanClass);
                            descriptors.put(bindingClass, descriptor);
                        } else if (descriptor.getHandler() == null) {
                            descriptor.setHandler(beanClass);
                        } else if (!descriptor.getHandler().equals(beanClass)) {
                            setDefinitionError(new IllegalStateException("Multiple handlers found for " + bindingClass.getName() + " (" + descriptor.getHandler().getName() + " and " + beanClass.getName() + ")"));
                        }
                    } else {
                        setDefinitionError(new IllegalStateException(beanClass.getName() + " is annotated with @" + bindingClass.getName() + " and therefore has to be " + "an abstract class, an interface or an implementation of " + InvocationHandler.class.getName()));
                    }
                } else {
                    pat.veto();
                    descriptor = descriptors.get(bindingClass);
                    if (descriptor == null) {
                        descriptor = new PartialBeanDescriptor(bindingClass, (Class<? extends InvocationHandler>) null, beanClass);
                        descriptors.put(bindingClass, descriptor);
                    } else if (!descriptor.getClasses().contains(beanClass)) {
                        descriptor.getClasses().add(beanClass);
                    }
                }
            }
        }
    }
}