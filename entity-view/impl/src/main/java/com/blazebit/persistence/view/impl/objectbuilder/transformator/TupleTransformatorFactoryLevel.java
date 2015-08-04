package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

class TupleTransformatorFactoryLevel {

    final List<TupleTransformerFactory> tupleTransformerFactories = new ArrayList<TupleTransformerFactory>();
    final List<TupleListTransformer> tupleListTransformers = new ArrayList<TupleListTransformer>();
}