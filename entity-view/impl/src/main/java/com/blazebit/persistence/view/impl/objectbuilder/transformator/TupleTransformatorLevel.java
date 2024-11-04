/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
class TupleTransformatorLevel {

    final TupleTransformer[] tupleTransformers;
    final TupleListTransformer tupleListTransformer;
    
    public TupleTransformatorLevel(TupleTransformer[] tupleTransformers,
            TupleListTransformer tupleListTransformer) {
        this.tupleTransformers = tupleTransformers;
        this.tupleListTransformer = tupleListTransformer;
    }
}