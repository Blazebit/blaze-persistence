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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryMethod;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.param.NamedParameter;
import org.apache.deltaspike.data.impl.param.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
public class Parameters {
    private static final Logger LOG = Logger.getLogger(Parameters.class.getName());

    private static final int DEFAULT_MAX = 0;
    private static final int DEFAULT_FIRST = -1;

    private final List<Parameter> parameterList;
    private final int max;
    private final int firstResult;

    private Parameters(List<Parameter> parameters, int max, int firstResult) {
        this.parameterList = parameters;
        this.max = max;
        this.firstResult = firstResult;
    }

    public static Parameters createEmpty() {
        List<Parameter> empty = Collections.emptyList();
        return new Parameters(empty, DEFAULT_MAX, DEFAULT_FIRST);
    }

    public static Parameters create(Method method, Object[] parameters, EntityViewRepositoryMethod repositoryMethod) {
        int max = extractSizeRestriction(method, repositoryMethod);
        int first = DEFAULT_FIRST;
        List<Parameter> result = new ArrayList<Parameter>(parameters.length);
        int paramIndex = 1;
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            if (isParameter(method.getParameterAnnotations()[i])) {
                QueryParam qpAnnotation = extractFrom(annotations[i], QueryParam.class);
                if (qpAnnotation != null) {
                    result.add(new NamedParameter(qpAnnotation.value(), parameters[i]));
                } else {
                    result.add(new NamedParameter("methodArg" + paramIndex++, parameters[i]));
                }
            } else {
                max = extractInt(parameters[i], annotations[i], MaxResults.class, max);
                first = extractInt(parameters[i], annotations[i], FirstResult.class, first);
            }
        }
        return new Parameters(result, max, first);
    }

    public void applyMapper(QueryInOutMapper<?> mapper) {
        for (Parameter param : parameterList) {
            param.applyMapper(mapper);
        }
    }

    public FullQueryBuilder<?, ?> applyTo(FullQueryBuilder<?, ?> queryBuilder) {
        for (Parameter param : parameterList) {
            if (param instanceof NamedParameter) {
                NamedParameter namedParameter = (NamedParameter) param;
                queryBuilder.setParameter(getParameterName(namedParameter), getParameterValue(param));
            } else {
                throw new UnsupportedOperationException("Blaze-Persistence criteria builder only supports named parameters");
            }
        }
        return queryBuilder;
    }

    private String getParameterName(NamedParameter namedParameter) {
        try {
            Field nameField = NamedParameter.class.getDeclaredField("name");
            nameField.setAccessible(true);
            return (String) nameField.get(namedParameter);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getParameterValue(Parameter parameter) {
        try {
            Method queryValueMethod = Parameter.class.getDeclaredMethod("queryValue");
            queryValueMethod.setAccessible(true);
            return queryValueMethod.invoke(parameter);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasSizeRestriction() {
        return max > DEFAULT_MAX;
    }

    public int getSizeRestriciton() {
        return max;
    }

    public boolean hasFirstResult() {
        return firstResult > DEFAULT_FIRST;
    }

    public int getFirstResult() {
        return firstResult;
    }

    private static int extractSizeRestriction(Method method, EntityViewRepositoryMethod repositoryMethod) {
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

    private static <A extends Annotation> int extractInt(Object parameter, Annotation[] annotations,
                                                         Class<A> target, int defaultVal) {
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