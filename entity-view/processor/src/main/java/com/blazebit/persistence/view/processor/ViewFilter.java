/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ViewFilter {

    private final String name;
    private final Map<String, String> optionalParameters;

    public ViewFilter(String name, TypeElement filterProvider, Context context) {
        this.name = name;
        Map<String, String> optionalParameters = new HashMap<>();
        for (Element enclosedElement : filterProvider.getEnclosedElements()) {
            if (enclosedElement instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                if (executableElement.getKind() == ElementKind.METHOD && "apply".equals(executableElement.getSimpleName().toString())) {
                    OptionalParameterScanner.scan(optionalParameters, executableElement, context);
                }
            }
        }

        this.optionalParameters = optionalParameters;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getOptionalParameters() {
        return optionalParameters;
    }
}
