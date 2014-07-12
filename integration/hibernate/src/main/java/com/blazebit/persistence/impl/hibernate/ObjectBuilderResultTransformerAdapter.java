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

package com.blazebit.persistence.impl.hibernate;

import com.blazebit.persistence.ObjectBuilder;
import java.util.List;
import org.hibernate.transform.ResultTransformer;

/**
 *
 * @author cpbec
 */
public class ObjectBuilderResultTransformerAdapter implements ResultTransformer {
    
    private final ObjectBuilder<?> builder;

    public ObjectBuilderResultTransformerAdapter(ObjectBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        return builder.build(tuple, aliases);
    }

    @Override
    public List transformList(List list) {
        return builder.buildList(list);
    }
}
