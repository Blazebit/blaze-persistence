/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
