/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.MetaAttribute;
import com.blazebit.persistence.view.processor.MetaConstructor;
import com.blazebit.persistence.view.processor.MetaEntityView;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaConstructor implements MetaConstructor {

    private final AnnotationMetaEntityView parent;
    private final ExecutableElement element;
    private final String name;
    private final List<MetaAttribute> parameters;

    public AnnotationMetaConstructor(AnnotationMetaEntityView parent) {
        this.parent = parent;
        this.element = null;
        this.name = "init";
        this.parameters = Collections.emptyList();
    }

    public AnnotationMetaConstructor(AnnotationMetaEntityView parent, ExecutableElement element, MetaAttributeGenerationVisitor visitor) {
        this.parent = parent;
        this.element = element;
        String name = "init";
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(Constants.VIEW_CONSTRUCTOR)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals("value")) {
                        name = entry.getValue().getValue().toString();
                        break;
                    }
                }
                break;
            }
        }
        this.name = name;
        List<MetaAttribute> parameters = new ArrayList<>(element.getParameters().size());
        List<? extends VariableElement> elementParameters = element.getParameters();
        for (int i = 0; i < elementParameters.size(); i++) {
            VariableElement parameter = elementParameters.get(i);
            AnnotationMetaAttribute result = parameter.asType().accept(visitor, parameter);
            result.setAttributeIndex(i);
            parameters.add(result);
        }
        this.parameters = parameters;
    }

    @Override
    public MetaEntityView getHostingEntity() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<MetaAttribute> getParameters() {
        return parameters;
    }
}
