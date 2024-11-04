/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.reflection.ReflectionUtils;

import jakarta.persistence.Query;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SplittingParameterTransformer implements ParameterValueTransformer {

    private final ParameterManager parameterManager;
    private final String[] parameterNames;
    private final Field[][] fields;
    private final Method[][] getters;

    public SplittingParameterTransformer(ParameterManager parameterManager, EntityMetamodel metamodel, Class<?> parameterType, Map<String, List<String>> parameterAccessPaths) {
        try {
            this.parameterManager = parameterManager;
            this.parameterNames = parameterAccessPaths.keySet().toArray(new String[parameterAccessPaths.size()]);
            Field[][] fields = new Field[parameterAccessPaths.size()][];
            Method[][] getters = new Method[parameterAccessPaths.size()][];

            int i = 0;
            for (List<String> accessPath : parameterAccessPaths.values()) {
                ManagedType<?> t = metamodel.getManagedType(parameterType);
                fields[i] = new Field[accessPath.size()];
                getters[i] = new Method[accessPath.size()];
                int j = 0;
                for (String property : accessPath) {
                    Attribute<?, ?> attribute = t.getAttribute(property);
                    Member member = attribute.getJavaMember();

                    if (member instanceof Method) {
                        Method getter = ReflectionUtils.getGetter(t.getJavaType(), attribute.getName());
                        getter.setAccessible(true);
                        getters[i][j++] = getter;
                    } else if (member instanceof Field) {
                        Field field = (Field) member;
                        field.setAccessible(true);
                        fields[i][j++] = field;
                    } else {
                        throw new IllegalArgumentException("Unsupported attribute member type [" + member + "] for attribute [" + attribute.getName() + "] of class [" + t.getJavaType().getName() + "]");
                    }
                    t = metamodel.getManagedType(JpaMetamodelUtils.resolveFieldClass(t.getJavaType(), attribute));
                }
                i++;
            }

            this.fields = fields;
            this.getters = getters;
        } catch (Exception e) {
            throw new IllegalArgumentException("The parameter splitter for the managed type [" + parameterType.getName() + "] could not be initialized!", e);
        }
    }

    @Override
    public ParameterValueTransformer forQuery(final Query query) {
        return new ParameterValueTransformer() {
            @Override
            public ParameterValueTransformer forQuery(Query query) {
                return SplittingParameterTransformer.this.forQuery(query);
            }

            @Override
            public Object transform(Object originalValue) {
                try {
                    for (int i = 0; i < parameterNames.length; i++) {
                        Object o = originalValue;
                        if (o != null) {
                            Field[] fieldAccess = fields[i];
                            Method[] methodAccess = getters[i];
                            for (int j = 0; j < fieldAccess.length; j++) {
                                if (fieldAccess[j] != null) {
                                    o = fieldAccess[j].get(o);
                                } else {
                                    o = methodAccess[j].invoke(o);
                                }
                                if (o == null) {
                                    break;
                                }
                            }
                        }
                        query.setParameter(parameterNames[i], o);
                    }
                    return originalValue;
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Could not split parameter value [" + originalValue + "]", ex);
                }
            }
        };
    }

    public String[] getParameterNames() {
        return parameterNames;
    }

    @Override
    public Object transform(Object originalValue) {
        try {
            for (int i = 0; i < parameterNames.length; i++) {
                Field[] fieldAccess = fields[i];
                Method[] methodAccess = getters[i];
                Object o = originalValue;
                for (int j = 0; j < fieldAccess.length; j++) {
                    if (fieldAccess[j] != null) {
                        o = fieldAccess[j].get(o);
                    } else {
                        o = methodAccess[j].invoke(o);
                    }
                    if (o == null) {
                        break;
                    }
                }
                parameterManager.satisfyParameter(parameterNames[i], o);
            }
            return originalValue;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not split parameter value [" + originalValue + "]", ex);
        }
    }
}
