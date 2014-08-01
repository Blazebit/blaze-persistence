/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class ParameterViewTypeObjectBuilder<T> extends AbstractViewTypeObjectBuilder<T> {
    
    private final String[] parameterMappings;
    private final QueryBuilder<?, ?> queryBuilder;

    public ParameterViewTypeObjectBuilder(ViewTypeObjectBuilderTemplate<T> template, QueryBuilder<?, ?> queryBuilder) {
        super(template);
        
        if (!template.hasParameters()) {
            throw new IllegalArgumentException("No templates without parameters allowed for this object builder!");
        }
        
        this.parameterMappings = template.getParameterMappings();
        this.queryBuilder = queryBuilder;
    }

    @Override
    public T build(Object[] tuple, String[] aliases) {
        for (int i = 0; i < parameterMappings.length; i++) {
            if (parameterMappings[i] != null) {
                tuple[i] = queryBuilder.getParameterValue(parameterMappings[i]);
            }
        }
        
        return super.build(tuple, aliases);
    }
}
