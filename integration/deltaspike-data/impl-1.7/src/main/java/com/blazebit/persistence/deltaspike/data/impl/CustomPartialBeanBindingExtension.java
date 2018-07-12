/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.deltaspike.data.impl;

import com.blazebit.apt.service.ServiceProvider;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.handler.QueryHandler;
import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;
import org.apache.deltaspike.partialbean.impl.PartialBeanDescriptor;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@ServiceProvider(Extension.class)
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