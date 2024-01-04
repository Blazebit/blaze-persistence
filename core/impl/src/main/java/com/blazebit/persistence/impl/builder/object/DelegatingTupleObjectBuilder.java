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

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.impl.SelectInfo;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class DelegatingTupleObjectBuilder extends TupleObjectBuilder {

    private final ObjectBuilder<Object[]> objectBuilder;

    public DelegatingTupleObjectBuilder(ObjectBuilder<Object[]> objectBuilder, List<SelectInfo> selectInfos, Map<String, Integer> selectAliasToPositionMap) {
        super(selectInfos, selectAliasToPositionMap);
        this.objectBuilder = objectBuilder;
    }

    @Override
    public Tuple build(Object[] tuple) {
        return super.build(objectBuilder.build(tuple));
    }
}
