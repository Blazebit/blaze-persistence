package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.List;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

class TupleTransformatorLevel {

    final List<TupleTransformer> tupleTransformers;
    final TupleListTransformer tupleListTransformer;
    
    public TupleTransformatorLevel(List<TupleTransformer> tupleTransformers,
            TupleListTransformer tupleListTransformer) {
        this.tupleTransformers = tupleTransformers;
        this.tupleListTransformer = tupleListTransformer;
    }
}