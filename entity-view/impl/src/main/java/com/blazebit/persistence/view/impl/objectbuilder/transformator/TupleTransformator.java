/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.*;

import com.blazebit.persistence.view.impl.objectbuilder.TupleRest;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TupleTransformator {

    private final List<TupleTransformatorLevel> transformatorLevels;

    public TupleTransformator(List<TupleTransformatorLevel> transformatorLevels) {
        this.transformatorLevels = transformatorLevels;
    }

    public List<Object[]> transformAll(List<Object[]> tupleList) {
        List<Object[]> newTupleList;
        UpdatableViewMap updatableViewMap = new UpdatableViewMap();

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
                    newTupleListIter.set(transform(i, tuple, updatableViewMap));
                }
            }
            newTupleList = transform(i, newTupleList);
        }

        // if we have multiple levels, we must filter duplicates afterwards
        if (transformatorLevels.size() > 1) {
            Set<TupleRest> tupleSet = new HashSet<>(newTupleList.size());

            Iterator<Object[]> tupleListIter = newTupleList.iterator();

            while (tupleListIter.hasNext()) {
                if (!tupleSet.add(new TupleRest(tupleListIter.next(), 0, 0))) {
                    tupleListIter.remove();
                }
            }
        }

        return newTupleList;
    }

    private Object[] transform(int level, Object[] tuple, UpdatableViewMap updatableViewMap) {
        List<TupleTransformer> tupleTransformers = transformatorLevels.get(level).tupleTransformers;
        Object[] currentTuple = tuple;
        for (int i = 0; i < tupleTransformers.size(); i++) {
            currentTuple = tupleTransformers.get(i).transform(currentTuple, updatableViewMap);
        }
        return currentTuple;
    }

    private List<Object[]> transform(int level, List<Object[]> tupleList) {
        TupleListTransformer tupleListTransformer = transformatorLevels.get(level).tupleListTransformer;
        if (tupleListTransformer == null) {
            return tupleList;
        }
        return tupleListTransformer.transform(tupleList);
    }
}