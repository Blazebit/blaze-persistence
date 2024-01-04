/*
 * Copyright 2014 - 2024 Blazebit.
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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class JavaTypeVariable {

    private final String name;
    private final boolean isExtends;
    private final JavaType bound;

    public JavaTypeVariable(TypeVariable typeVariable) {
        this.name = typeVariable.toString();
        this.isExtends = typeVariable.getLowerBound().getKind() == TypeKind.NULL;
        if (isExtends) {
            this.bound = new JavaType(typeVariable.getUpperBound());
        } else {
            this.bound = new JavaType(typeVariable.getLowerBound());
        }
    }

    public String getName() {
        return name;
    }

    public void append(ImportContext importContext, StringBuilder sb) {
        sb.append(name);
        if (isExtends) {
            sb.append(" extends ");
        } else {
            sb.append(" super ");
        }
        bound.append(importContext, sb);
    }
}
