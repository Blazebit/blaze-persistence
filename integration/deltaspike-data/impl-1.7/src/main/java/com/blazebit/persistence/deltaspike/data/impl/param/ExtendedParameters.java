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

package com.blazebit.persistence.deltaspike.data.impl.param;

import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.OffsetPageRequest;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.impl.param.IndexedParameter;
import org.apache.deltaspike.data.impl.param.NamedParameter;
import org.apache.deltaspike.data.impl.param.Parameter;

import javax.persistence.Query;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.param.Parameters} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ExtendedParameters {
    private static final Logger LOG = Logger.getLogger(ExtendedParameters.class.getName());

    private static final int DEFAULT_MAX = Integer.MAX_VALUE;
    private static final int DEFAULT_FIRST = 0;

    private final List<Parameter> parameterList;
    private final Pageable pageable;
    private final Specification<?> specification;

    private ExtendedParameters(List<Parameter> parameters, Pageable pageable, Specification<?> specification) {
        this.parameterList = parameters;
        this.pageable = pageable;
        this.specification = specification;
    }

    public static ExtendedParameters createEmpty() {
        List<Parameter> empty = Collections.emptyList();
        return new ExtendedParameters(empty, new PageRequest(null, DEFAULT_FIRST, DEFAULT_MAX), null);
    }

    public static ExtendedParameters create(Method method, Object[] parameters, RepositoryMethod repositoryMethod) {
        int max = extractSizeRestriction(method, repositoryMethod);
        int first = DEFAULT_FIRST;
        List<Parameter> result = new ArrayList<Parameter>(parameters.length);
        int paramIndex = 1;
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Pageable pageable = null;
        Specification<?> specification = null;
        for (int i = 0; i < parameters.length; i++) {
            // Extended for support of Pageable and Specification parameters
            if (Pageable.class.isAssignableFrom(parameterTypes[i])) {
                pageable = (Pageable) parameters[i];
            } else if (Specification.class.isAssignableFrom(parameterTypes[i])) {
                specification = (Specification) parameters[i];
            } else if (isParameter(annotations[i])) {
                QueryParam qpAnnotation = extractFrom(annotations[i], QueryParam.class);
                if (qpAnnotation != null) {
                    result.add(new NamedParameter(qpAnnotation.value(), parameters[i]));
                } else {
                    result.add(new IndexedParameter(paramIndex++, parameters[i]));
                }
            } else {
                max = extractInt(parameters[i], annotations[i], MaxResults.class, max);
                first = extractInt(parameters[i], annotations[i], FirstResult.class, first);
            }
        }
        if (pageable == null) {
            if (first > DEFAULT_FIRST || max > 0 && max < Integer.MAX_VALUE) {
                pageable = new OffsetPageRequest(first, max);
            }
        }
        return new ExtendedParameters(result, pageable, specification);
    }

    public Pageable getPageable() {
        return pageable;
    }

    public Specification<?> getSpecification() {
        return specification;
    }

    public void applyMapper(QueryInOutMapper<?> mapper) {
        for (Parameter param : parameterList) {
            param.applyMapper(mapper);
        }
    }

    public Query applyTo(Query query) {
        for (Parameter param : parameterList) {
            param.apply(query);
        }
        return query;
    }

    private static int extractSizeRestriction(Method method, RepositoryMethod repositoryMethod) {
        if (method.isAnnotationPresent(org.apache.deltaspike.data.api.Query.class)) {
            return method.getAnnotation(org.apache.deltaspike.data.api.Query.class).max();
        }
        return repositoryMethod.getDefinedMaxResults();
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A extractFrom(Annotation[] annotations, Class<A> target) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(target)) {
                return (A) annotation;
            }
        }
        return null;
    }

    private static <A extends Annotation> int extractInt(Object parameter, Annotation[] annotations, Class<A> target, int defaultVal) {
        if (parameter != null) {
            A result = extractFrom(annotations, target);
            if (result != null) {
                if (parameter instanceof Integer) {
                    return (Integer) parameter;
                } else {
                    LOG.log(Level.WARNING, "Method parameter extraction: " +
                                    "Param type must be int: {0}->is:{1}",
                            new Object[]{target, parameter.getClass()});
                }
            }
        }
        return defaultVal;
    }

    private static boolean isParameter(Annotation[] annotations) {
        return extractFrom(annotations, MaxResults.class) == null &&
                extractFrom(annotations, FirstResult.class) == null;
    }

}