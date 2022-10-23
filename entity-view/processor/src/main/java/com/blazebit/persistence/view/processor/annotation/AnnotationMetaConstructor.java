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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.MetaAttribute;
import com.blazebit.persistence.view.processor.MetaConstructor;
import com.blazebit.persistence.view.processor.MetaEntityView;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaConstructor implements MetaConstructor {

    private final AnnotationMetaEntityView parent;
    private final boolean isReal;
    private final boolean hasSelfParameter;
    private final String name;
    private final List<MetaAttribute> parameters;
    private final Map<String, String> optionalParameters;

    public AnnotationMetaConstructor(AnnotationMetaEntityView parent, Map<String, TypeMirror> parentOptionalParameters) {
        this.parent = parent;
        this.name = "init";
        this.parameters = Collections.emptyList();
        this.hasSelfParameter = false;
        boolean isReal = false;
        TypeMirror superclassMirror = parent.getTypeElement().getSuperclass();
        while (superclassMirror.getKind() != TypeKind.NONE) {
            TypeElement superclass = (TypeElement) ((DeclaredType) superclassMirror).asElement();
            if ("java.lang.Object".equals(superclass.getQualifiedName().toString())) {
                break;
            }
            for (Element enclosedElement : superclass.getEnclosedElements()) {
                String name = enclosedElement.getSimpleName().toString();
                if ("<init>".equals(name)) {
                    isReal = true;
                    break;
                }
            }
            if (isReal) {
                break;
            }
            superclassMirror = superclass.getSuperclass();
        }
        this.isReal = isReal;
        Map<String, String> optionalParameters = new TreeMap<>();
        for (Map.Entry<String, TypeMirror> entry : parentOptionalParameters.entrySet()) {
            optionalParameters.put(entry.getKey(), entry.getValue().toString());
        }
        this.optionalParameters = optionalParameters;
    }

    public AnnotationMetaConstructor(AnnotationMetaEntityView parent, Map<String, TypeMirror> parentOptionalParameters, Map<String, TypeMirror> optionalParameters, ExecutableElement element, MetaAttributeGenerationVisitor visitor, Context context) {
        this.parent = parent;
        this.isReal = true;
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
        boolean hasSelfParameter = false;
        for (int i = 0; i < elementParameters.size(); i++) {
            VariableElement parameter = elementParameters.get(i);
            AnnotationMetaAttribute result = parameter.asType().accept(visitor, parameter);
            result.setAttributeIndex(i);
            parameters.add(result);
            for (Map.Entry<String, TypeMirror> entry : result.getOptionalParameters().entrySet()) {
                TypeMirror typeElement = entry.getValue();
                TypeMirror existingTypeElement = optionalParameters.get(entry.getKey());
                if (existingTypeElement == null || context.getTypeUtils().isAssignable(typeElement, existingTypeElement)) {
                    optionalParameters.put(entry.getKey(), entry.getValue());
                }
            }
            result.getOptionalParameters().clear();
            hasSelfParameter = hasSelfParameter || result.isSelf();
        }
        this.parameters = parameters;
        this.hasSelfParameter = hasSelfParameter;
        this.optionalParameters = createOrderedOptionalParameters(parentOptionalParameters, context, optionalParameters);
    }

    private static Map<String, String> createOrderedOptionalParameters(Map<String, TypeMirror> entityOptionalParameters, Context context, Map<String, TypeMirror> elementOptionalParameters) {
        Map<String, String> optionalParameters = new TreeMap<>();
        for (Map.Entry<String, TypeMirror> entry : elementOptionalParameters.entrySet()) {
            if (!context.getOptionalParameters().containsKey(entry.getKey())) {
                TypeMirror typeElement = entityOptionalParameters.get(entry.getKey());
                if (typeElement == null) {
                    optionalParameters.put(entry.getKey(), entry.getValue().toString());
                } else {
                    if (context.getTypeUtils().isAssignable(typeElement, entry.getValue())) {
                        optionalParameters.put(entry.getKey(), typeElement.toString());
                    } else {
                        optionalParameters.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        }
        for (Map.Entry<String, TypeMirror> entry : entityOptionalParameters.entrySet()) {
            if (!context.getOptionalParameters().containsKey(entry.getKey())) {
                optionalParameters.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return optionalParameters;
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
    public boolean isReal() {
        return isReal;
    }

    @Override
    public boolean hasSelfParameter() {
        return hasSelfParameter;
    }

    @Override
    public List<MetaAttribute> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, String> getOptionalParameters() {
        return optionalParameters;
    }
}
