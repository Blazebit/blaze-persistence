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

import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TupleTransformator {

    private final List<TupleTransformatorLevel> transformatorLevels = new ArrayList<TupleTransformatorLevel>();
    private int currentLevel = 0;

    public TupleTransformator() {
        transformatorLevels.add(new TupleTransformatorLevel());
    }

    public boolean hasTransformers() {
        return transformatorLevels.get(0).tupleListTransformers.size() > 0
            || transformatorLevels.get(0).tupleTransformers.size() > 0;
    }

    public List<Object[]> transformAll(List<Object[]> tupleList) {
        List<Object[]> newTupleList;

        // Performance optimization
        // LinkedList can remove elements from the list very fast
        // This is important because transformers avoid copying of tuples and instead remove elements from the tupleList
        if (tupleList instanceof LinkedList<?>) {
            newTupleList = tupleList;
        } else {
            newTupleList = new LinkedList(tupleList);
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

    public void add(TupleTransformator tupleTransformator) {
        if (!tupleTransformator.hasTransformers()) {
            return;
        }

        for (int i = 0; i < tupleTransformator.transformatorLevels.size(); i++) {
            if (i != 0) {
                incrementLevel();
            }

            TupleTransformatorLevel thisLevel = transformatorLevels.get(currentLevel);
            TupleTransformatorLevel otherLevel = tupleTransformator.transformatorLevels.get(i);
            thisLevel.tupleTransformers.addAll(otherLevel.tupleTransformers);
            thisLevel.tupleListTransformers.addAll(otherLevel.tupleListTransformers);
        }
    }

    private void incrementLevel() {
        currentLevel++;
        transformatorLevels.add(new TupleTransformatorLevel());
    }

    public void add(TupleListTransformer tupleListTransformer) {
        transformatorLevels.get(currentLevel).tupleListTransformers.add(tupleListTransformer);
        incrementLevel();
    }

    public void add(TupleTransformer tupleTransformer) {
        transformatorLevels.get(currentLevel).tupleTransformers.add(tupleTransformer);
    }

    public void init(QueryBuilder<?, ?> queryBuilder) {
        for (TupleTransformatorLevel thisLevel : transformatorLevels) {
            for (TupleTransformer t : thisLevel.tupleTransformers) {
                t.init(queryBuilder);
            }
        }
    }

    private static class TupleTransformatorLevel {

        private final List<TupleTransformer> tupleTransformers = new ArrayList<TupleTransformer>();
        private final List<TupleListTransformer> tupleListTransformers = new ArrayList<TupleListTransformer>();
    }
}
