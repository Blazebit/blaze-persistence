/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class OptionalParameterUtils {

    private OptionalParameterUtils() {
    }

    public static void addOptionalParameters(Map<String, String> optionalParameters, String mapping, Context context) {
        addOptionalParameters(
            new OptionalParameterConsumer() {
                @Override
                public void addOptionalParameter(String name, TypeMirror typeMirror) {
                    if (!optionalParameters.containsKey(name)) {
                        if (typeMirror == null) {
                            typeMirror = context.getTypeElement("java.lang.Object").asType();
                        }
                        optionalParameters.put(name, typeMirror.toString());
                    }
                }
            },
            mapping,
            context
        );
    }

    public static void addOptionalParametersTypeElement(Map<String, TypeMirror> optionalParameters, String mapping, Context context) {
        addOptionalParameters(
            new OptionalParameterConsumer() {
                @Override
                public void addOptionalParameter(String name, TypeMirror typeMirror) {
                    if (!optionalParameters.containsKey(name)) {
                        if (typeMirror == null) {
                            typeMirror = context.getTypeElement("java.lang.Object").asType();
                        }
                        optionalParameters.put(name, typeMirror);
                    }
                }
            },
            mapping,
            context
        );
    }

    private static void addOptionalParameters(OptionalParameterConsumer optionalParameters, String mapping, Context context) {
        if (mapping == null || mapping.isEmpty()) {
            return;
        }
        int idx = -1;
        StringBuilder sb = null;
        TypeElement objectTypeElement = null;
        while ((idx = mapping.indexOf(':', idx + 1)) != -1) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.setLength(0);
            }
            for (int i = idx + 1; i < mapping.length(); i++) {
                final char c = mapping.charAt(i);
                if (Character.isJavaIdentifierPart(c)) {
                    sb.append(c);
                } else {
                    break;
                }
            }
            if (sb.length() != 0) {
                String name = sb.toString();
                TypeMirror existingTypeElement = context.getOptionalParameters().get(name);
                optionalParameters.addOptionalParameter(name, existingTypeElement);
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.6.8
     */
    private static interface OptionalParameterConsumer {
        void addOptionalParameter(String name, TypeMirror typeMirror);
    }
}
