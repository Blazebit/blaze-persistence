/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewListeners;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewComponentProvider extends ClassPathScanningCandidateComponentProvider {

    public EntityViewComponentProvider(Iterable<? extends TypeFilter> includeFilters) {
        super(false);

        Assert.notNull(includeFilters,"includeFilters cannot be null");

        if (includeFilters.iterator().hasNext()) {
            for (TypeFilter filter : includeFilters) {
                addIncludeFilter(filter);
            }
        } else {
            super.addIncludeFilter(new AnnotationTypeFilter(EntityView.class, false, false));
            super.addIncludeFilter(new AnnotationTypeFilter(EntityViewListener.class, false, false));
            super.addIncludeFilter(new AnnotationTypeFilter(EntityViewListeners.class, false, false));
        }
        super.addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                return !metadataReader.getAnnotationMetadata().hasAnnotation(EntityView.class.getName())
                        && !metadataReader.getAnnotationMetadata().hasAnnotation(EntityViewListener.class.getName())
                        && !metadataReader.getAnnotationMetadata().hasAnnotation(EntityViewListeners.class.getName());
            }
        });
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isIndependent();
    }
}
