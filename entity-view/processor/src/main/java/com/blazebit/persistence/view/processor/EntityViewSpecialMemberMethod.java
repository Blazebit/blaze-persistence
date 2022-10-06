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

import javax.lang.model.element.ExecutableElement;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class EntityViewSpecialMemberMethod {

    private final String name;
    private final String returnTypeName;

    public EntityViewSpecialMemberMethod(ExecutableElement postCreate) {
        this.name = postCreate.getSimpleName().toString();
        this.returnTypeName = postCreate.getReturnType().toString();
    }

    public String getName() {
        return name;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }
}
