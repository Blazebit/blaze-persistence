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

package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import com.blazebit.persistence.view.impl.objectbuilder.TupleRest;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
        UpdatableViewMap updatableViewMap = new UpdatableViewMap();

        for (int i = 0; i < transformatorLevels.size(); i++) {
            if (transformatorLevels.get(i).tupleTransformers.length != 0) {
                ListIterator<Object[]> newTupleListIter = tupleList.listIterator();

                while (newTupleListIter.hasNext()) {
                    Object[] tuple = newTupleListIter.next();
                    newTupleListIter.set(transform(i, tuple, updatableViewMap));
                }
            }
            tupleList = transform(i, tupleList);
        }

        // if we have multiple levels, we must filter duplicates afterwards
        if (transformatorLevels.size() > 1) {
            Set<TupleRest> tupleSet = new HashSet<>(tupleList.size());

            Iterator<Object[]> tupleListIter = tupleList.iterator();

            while (tupleListIter.hasNext()) {
                if (!tupleSet.add(new TupleRest(tupleListIter.next(), 0, 0))) {
                    tupleListIter.remove();
                }
            }
        }

        return tupleList;
    }

    public Object[] transform(Object[] tuple) {
        if (transformatorLevels.size() != 1) {
            throw new IllegalStateException("Can only do single transformations if there is only a single level");
        }
        UpdatableViewMap updatableViewMap = new UpdatableViewMap();
        return transform(0, tuple, updatableViewMap);
    }

    private Object[] transform(int level, Object[] tuple, UpdatableViewMap updatableViewMap) {
        TupleTransformer[] tupleTransformers = transformatorLevels.get(level).tupleTransformers;
        Object[] currentTuple = tuple;
        for (int i = 0; i < tupleTransformers.length; i++) {
            currentTuple = tupleTransformers[i].transform(currentTuple, updatableViewMap);
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