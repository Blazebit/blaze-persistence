/*
 * Copyright 2014 - 2023 Blazebit.
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class ForeignPackageType {

    private final String name;
    private final String simpleName;
    private final List<JavaTypeVariable> typeVariables;
    private final List<ForeignPackageMethod> methods;

    public ForeignPackageType(MetaEntityView entity, TypeElement typeElement, Context context) {
        this.name = typeElement.getQualifiedName().toString();
        this.simpleName = typeElement.getSimpleName().toString();
        List<TypeVariable> typeParameters = (List<TypeVariable>) ((DeclaredType) typeElement.asType()).getTypeArguments();
        if (typeParameters.isEmpty()) {
            this.typeVariables = Collections.emptyList();
        } else {
            List<JavaTypeVariable> typeVariables = new ArrayList<>(typeParameters.size());
            for (TypeVariable typeVariable : typeParameters) {
                typeVariables.add(new JavaTypeVariable(typeVariable));
            }
            this.typeVariables = typeVariables;
        }
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        List<ForeignPackageMethod> methods = new ArrayList<>(enclosedElements.size());
        for (Element element : enclosedElements) {
            Set<Modifier> modifiers = element.getModifiers();
            if (element instanceof ExecutableElement && modifiers.contains(Modifier.ABSTRACT) && !modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PRIVATE) && element.getKind() == ElementKind.METHOD) {
                methods.add(new ForeignPackageMethod(entity, (ExecutableElement) element, context));
            }
        }
        this.methods = methods;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public List<JavaTypeVariable> getTypeVariables() {
        return typeVariables;
    }

    public List<ForeignPackageMethod> getMethods() {
        return methods;
    }
}
