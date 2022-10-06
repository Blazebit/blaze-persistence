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
