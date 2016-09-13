package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

class TupleTransformatorFactoryLevel {

    final List<TupleTransformerFactory> tupleTransformerFactories = new ArrayList<TupleTransformerFactory>();
    TupleListTransformer tupleListTransformer;
    TupleListTransformerFactory tupleListTransformerFactory;
}