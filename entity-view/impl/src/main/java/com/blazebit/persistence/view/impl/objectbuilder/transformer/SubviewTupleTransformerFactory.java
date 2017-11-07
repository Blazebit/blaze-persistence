/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Map;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SubviewTupleTransformerFactory implements TupleTransformerFactory {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final boolean updatable;
    private final boolean nullIfEmpty;

    public SubviewTupleTransformerFactory(ViewTypeObjectBuilderTemplate<Object[]> template, boolean updatable, boolean nullIfEmpty) {
        this.template = template;
        this.updatable = updatable;
        this.nullIfEmpty = nullIfEmpty;
    }

    @Override
    public TupleTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        ObjectBuilder<Object[]> objectBuilder = template.createObjectBuilder(queryBuilder, optionalParameters, entityViewConfiguration, true, nullIfEmpty);
        if (updatable) {
            return new UpdatableSubviewTupleTransformer(template, objectBuilder);
        } else {
            return new SubviewTupleTransformer(template, objectBuilder);
        }
    }

}
