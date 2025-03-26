/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import kotlin.Metadata;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KCallable;
import kotlin.reflect.KClass;
import kotlin.reflect.KProperty;
import kotlin.reflect.KTypeProjection;

import java.lang.reflect.Method;

/**
 * A support class to interact with Kotlin types.
 *
 * @author Christian Beikov
 * @since 1.6.15
 */
class KotlinSupport {

    private static final KotlinSupportImpl INSTANCE;

    static {
        KotlinSupportImpl instance;
        try {
            // Trigger calling Kotlin code to see if Kotlin is on the class path
            JvmClassMappingKt.getKotlinClass(Integer.class);
            instance = new KotlinSupportImpl();
        } catch (Throwable e) {
            // Ignore
            instance = null;
        }

        INSTANCE = instance;
    }

    private KotlinSupport() {
    }

    /**
     * @author Christian Beikov
     * @since 1.6.15
     */
    private static class KotlinSupportImpl {
        boolean isKotlinNotNull(Method method) {
            if (method.getDeclaringClass().getAnnotation(Metadata.class) != null) {
                KClass<?> kotlinClass = JvmClassMappingKt.getKotlinClass(method.getDeclaringClass());
                String propertyName = null;
                for (KCallable<?> member : kotlinClass.getMembers()) {
                    if (member instanceof KProperty<?>) {
                        if (propertyName == null) {
                            String methodName = method.getName();
                            if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2))) {
                                propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                            } else if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
                                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                            } else {
                                propertyName = methodName;
                            }
                        }
                        if (propertyName.equals(member.getName())) {
                            return !member.getReturnType().isMarkedNullable();
                        }
                    } else if (member.getParameters().isEmpty() && method.getName().equals(member.getName())) {
                        return !member.getReturnType().isMarkedNullable();
                    }
                }
            }
            return false;
        }

        boolean isKotlinTypeArgumentNotNull(Method method, int typeArgumentIndex) {
            if (method.getDeclaringClass().getAnnotation(Metadata.class) != null) {
                KClass<?> kotlinClass = JvmClassMappingKt.getKotlinClass(method.getDeclaringClass());
                String propertyName = null;
                for (KCallable<?> member : kotlinClass.getMembers()) {
                    if (member instanceof KProperty<?>) {
                        if (propertyName == null) {
                            String methodName = method.getName();
                            if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2))) {
                                propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                            } else if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
                                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                            } else {
                                propertyName = methodName;
                            }
                        }
                        if (propertyName.equals(member.getName())) {
                            KTypeProjection kTypeProjection = member.getReturnType().getArguments().get(typeArgumentIndex);
                            return kTypeProjection.getType() != null && !kTypeProjection.getType().isMarkedNullable();
                        }
                    } else if (member.getParameters().isEmpty() && method.getName().equals(member.getName())) {
                        KTypeProjection kTypeProjection = member.getReturnType().getArguments().get(typeArgumentIndex);
                        return kTypeProjection.getType() != null && !kTypeProjection.getType().isMarkedNullable();
                    }
                }
            }
            return false;
        }
    }

    static boolean isKotlinNotNull(Method method) {
        return INSTANCE != null && INSTANCE.isKotlinNotNull(method);
    }

    static boolean isKotlinTypeArgumentNotNull(Method method, int typeArgumentIndex) {
        return INSTANCE != null && INSTANCE.isKotlinTypeArgumentNotNull(method, typeArgumentIndex);
    }

}
