/*
 * Copyright 2014 - 2021 Blazebit.
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

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaVersionAttribute extends AnnotationMetaAttribute {

    public AnnotationMetaVersionAttribute(AnnotationMetaEntityView parent, Context context) {
        super(parent, context);
    }

    @Override
    public final String getMetaType() {
        return Constants.SINGULAR_ATTRIBUTE;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }
}
