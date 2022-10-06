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

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class EntityIdAttribute {

    private final String name;
    private final ElementKind kind;
    private final String idAttributeName;
    private final String idMethodName;

    public EntityIdAttribute(Element entityIdAttribute) {
        this.name = entityIdAttribute.getSimpleName().toString();
        this.kind = entityIdAttribute.getKind();
        if (entityIdAttribute.getKind() == ElementKind.METHOD) {
            String methodName = entityIdAttribute.getSimpleName().toString();
            if (methodName.startsWith("is")) {
                this.idAttributeName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            } else {
                this.idAttributeName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
        } else {
            this.idAttributeName = entityIdAttribute.getSimpleName().toString();
        }
        this.idMethodName = "$$_" + entityIdAttribute.getEnclosingElement().getSimpleName().toString() + "_" + idAttributeName;
    }

    public String getName() {
        return name;
    }

    public ElementKind getKind() {
        return kind;
    }

    public String getIdAttributeName() {
        return idAttributeName;
    }

    public String getIdMethodName() {
        return idMethodName;
    }
}
