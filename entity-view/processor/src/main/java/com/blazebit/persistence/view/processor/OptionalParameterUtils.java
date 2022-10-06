/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.TypeElement;
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
                public void addOptionalParameter(String name, TypeElement typeElement) {
                    if (!optionalParameters.containsKey(name)) {
                        if (typeElement == null) {
                            typeElement = context.getTypeElement("java.lang.Object");
                        }
                        optionalParameters.put(name, typeElement.getQualifiedName().toString());
                    }
                }
            },
            mapping,
            context
        );
    }

    public static void addOptionalParametersTypeElement(Map<String, TypeElement> optionalParameters, String mapping, Context context) {
        addOptionalParameters(
            new OptionalParameterConsumer() {
                @Override
                public void addOptionalParameter(String name, TypeElement typeElement) {
                    if (!optionalParameters.containsKey(name)) {
                        if (typeElement == null) {
                            typeElement = context.getTypeElement("java.lang.Object");
                        }
                        optionalParameters.put(name, typeElement);
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
                TypeElement existingTypeElement = context.getOptionalParameters().get(name);
                optionalParameters.addOptionalParameter(name, existingTypeElement);
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.6.8
     */
    private static interface OptionalParameterConsumer {
        void addOptionalParameter(String name, TypeElement typeElement);
    }
}
