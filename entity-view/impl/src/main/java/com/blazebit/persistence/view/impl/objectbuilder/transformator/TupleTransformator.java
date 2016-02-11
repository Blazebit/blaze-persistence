package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

public class TupleTransformator {
	
    private final List<TupleTransformatorLevel> transformatorLevels;

	public TupleTransformator(List<TupleTransformatorLevel> transformatorLevels) {
		this.transformatorLevels = transformatorLevels;
	}

	public List<Object[]> transformAll(List<Object[]> tupleList) {
        List<Object[]> newTupleList;

        // Performance optimization
        // LinkedList can remove elements from the list very fast
        // This is important because transformers avoid copying of tuples and instead remove elements from the tupleList
        if (tupleList instanceof LinkedList<?>) {
            newTupleList = tupleList;
        } else {
            newTupleList = new LinkedList<Object[]>(tupleList);
        }

        for (int i = 0; i < transformatorLevels.size(); i++) {
                if (!transformatorLevels.get(i).tupleTransformers.isEmpty()) {
                ListIterator<Object[]> newTupleListIter = newTupleList.listIterator();

                while (newTupleListIter.hasNext()) {
                    Object[] tuple = newTupleListIter.next();
                    newTupleListIter.set(transform(i, tuple));
                }
            }
            newTupleList = transform(i, newTupleList);
        }
        return newTupleList;
    }

    private Object[] transform(int level, Object[] tuple) {
        List<TupleTransformer> tupleTransformers = transformatorLevels.get(level).tupleTransformers;
        Object[] currentTuple = tuple;
        for (int i = 0; i < tupleTransformers.size(); i++) {
            currentTuple = tupleTransformers.get(i).transform(currentTuple);
        }
        return currentTuple;
    }

    private List<Object[]> transform(int level, List<Object[]> tupleList) {
        List<TupleListTransformer> tupleListTransformers = transformatorLevels.get(level).tupleListTransformers;
        List<Object[]> currentTuples = tupleList;
        for (int i = 0; i < tupleListTransformers.size(); i++) {
            currentTuples = tupleListTransformers.get(i).transform(currentTuples);
        }
        return currentTuples;
    }
}