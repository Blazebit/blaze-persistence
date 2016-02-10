package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.List;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

class TupleTransformatorLevel {

    final List<TupleTransformer> tupleTransformers;
    final List<TupleListTransformer> tupleListTransformers;
    
	public TupleTransformatorLevel(List<TupleTransformer> tupleTransformers,
			List<TupleListTransformer> tupleListTransformers) {
		this.tupleTransformers = tupleTransformers;
		this.tupleListTransformers = tupleListTransformers;
	}
}