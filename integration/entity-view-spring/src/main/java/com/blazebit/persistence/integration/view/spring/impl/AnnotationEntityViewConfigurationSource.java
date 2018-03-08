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

package com.blazebit.persistence.integration.view.spring.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class AnnotationEntityViewConfigurationSource extends AbstractEntityViewConfigurationSource {

    private static final String BASE_PACKAGES = "basePackages";
    private static final String BASE_PACKAGE_CLASSES = "basePackageClasses";

    private final AnnotationMetadata configMetadata;
    private final AnnotationAttributes attributes;
    private final ResourceLoader resourceLoader;

    public AnnotationEntityViewConfigurationSource(AnnotationMetadata metadata, Class<? extends Annotation> annotation,
                                                   ResourceLoader resourceLoader, Environment environment) {
        super(environment);
        Assert.notNull(metadata);
        Assert.notNull(annotation);
        Assert.notNull(resourceLoader);

        this.attributes = new AnnotationAttributes(metadata.getAnnotationAttributes(annotation.getName()));
        this.configMetadata = metadata;
        this.resourceLoader = resourceLoader;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationSource#getBasePackages()
     */
    @Override
    public Iterable<String> getBasePackages() {

        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray(BASE_PACKAGES);
        Class<?>[] basePackageClasses = attributes.getClassArray(BASE_PACKAGE_CLASSES);

        // Default configuration - return package of annotated class
        if (value.length == 0 && basePackages.length == 0 && basePackageClasses.length == 0) {
            String className = configMetadata.getClassName();
            return Collections.singleton(ClassUtils.getPackageName(className));
        }

        Set<String> packages = new HashSet<String>();
        packages.addAll(Arrays.asList(value));
        packages.addAll(Arrays.asList(basePackages));

        for (Class<?> typeName : basePackageClasses) {
            packages.add(ClassUtils.getPackageName(typeName));
        }

        return packages;
    }

    protected Iterable<TypeFilter> getIncludeFilters() {
        return parseFilters("includeFilters");
    }

    protected Iterable<TypeFilter> getExcludeFilters() {
        return parseFilters("excludeFilters");
    }

    private Set<TypeFilter> parseFilters(String attributeName) {
        Set<TypeFilter> result = new HashSet<>();
        AnnotationAttributes[] filters = attributes.getAnnotationArray(attributeName);

        for (AnnotationAttributes filter : filters) {
            result.addAll(typeFiltersFor(filter));
        }

        return result;
    }

    /**
     * Copy of {@code ComponentScanAnnotationParser#typeFiltersFor}.
     *
     * @param filterAttributes
     * @return
     */
    private List<TypeFilter> typeFiltersFor(AnnotationAttributes filterAttributes) {

        List<TypeFilter> typeFilters = new ArrayList<TypeFilter>();
        FilterType filterType = filterAttributes.getEnum("type");

        for (Class<?> filterClass : filterAttributes.getClassArray("value")) {
            switch (filterType) {
                case ANNOTATION:
                    Assert.isAssignable(Annotation.class, filterClass, "An error occured when processing a @ComponentScan "
                            + "ANNOTATION type filter: ");
                    @SuppressWarnings("unchecked")
                    Class<Annotation> annoClass = (Class<Annotation>) filterClass;
                    typeFilters.add(new AnnotationTypeFilter(annoClass));
                    break;
                case ASSIGNABLE_TYPE:
                    typeFilters.add(new AssignableTypeFilter(filterClass));
                    break;
                case CUSTOM:
                    Assert.isAssignable(TypeFilter.class, filterClass, "An error occured when processing a @ComponentScan "
                            + "CUSTOM type filter: ");
                    typeFilters.add(BeanUtils.instantiateClass(filterClass, TypeFilter.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown filter type " + filterType);
            }
        }

        for (String expression : getPatterns(filterAttributes)) {

            String rawName = filterType.toString();

            if ("REGEX".equals(rawName)) {
                typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(expression)));
            } else if ("ASPECTJ".equals(rawName)) {
                typeFilters.add(new AspectJTypeFilter(expression, this.resourceLoader.getClassLoader()));
            } else {
                throw new IllegalArgumentException("Unknown filter type " + filterType);
            }
        }

        return typeFilters;
    }

    /**
     * Safely reads the {@code pattern} attribute from the given {@link AnnotationAttributes} and returns an empty list if
     * the attribute is not present.
     *
     * @param filterAttributes must not be {@literal null}.
     * @return
     */
    private String[] getPatterns(AnnotationAttributes filterAttributes) {

        try {
            return filterAttributes.getStringArray("pattern");
        } catch (IllegalArgumentException o_O) {
            return new String[0];
        }
    }

}
