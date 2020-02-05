/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.view.impl.objectbuilder.mapper.MultisetList;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformator;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class AbstractMultisetTupleListTransformer<C> extends TupleListTransformer {

    private final boolean hasSelectOrSubselectFetchedAttributes;
    private final TupleTransformator tupleTransformator;
    private final TupleTransformer subviewTupleTransformer;
    private final BasicUserTypeStringSupport<Object>[] fieldConverters;
    private final TypeConverter<Object, Object> elementConverter;

    public AbstractMultisetTupleListTransformer(int startIndex, boolean hasSelectOrSubselectFetchedAttributes, TupleTransformator tupleTransformator, TupleTransformer subviewTupleTransformer, BasicUserTypeStringSupport<Object>[] fieldConverters, TypeConverter<Object, Object> elementConverter) {
        super(startIndex);
        this.hasSelectOrSubselectFetchedAttributes = hasSelectOrSubselectFetchedAttributes;
        this.tupleTransformator = tupleTransformator;
        this.subviewTupleTransformer = subviewTupleTransformer;
        this.elementConverter = elementConverter;
        this.fieldConverters = fieldConverters;
    }

    @Override
    public int getConsumableIndex() {
        return -1;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        Iterator<Object[]> tupleListIter = tuples.iterator();
        // We differentiate here because for SELECT or SUBSELECT fetching we want to apply the tuple transformator on a multiset of the nested sets
        // This is done so that caching for correlation keys works properly, which isn't necessary for other fetch strategies
        if (hasSelectOrSubselectFetchedAttributes) {
            MultisetList nestedTuples = new MultisetList(startIndex, tuples);
            int index = 0;

            // First, go through all multi-sets and convert the fields
            // While doing that, also collect the multi-set sizes for nested processing
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                if (tuple[startIndex] != null) {
                    List<Object> objects = (List<Object>) tuple[startIndex];
                    for (int i = 0; i < objects.size(); i++) {
                        Object[] elementTuple = (Object[]) objects.get(i);
                        for (int j = 0; j < fieldConverters.length; j++) {
                            if (elementTuple[j] instanceof CharSequence) {
                                elementTuple[j] = fieldConverters[j].fromString((CharSequence) elementTuple[j]);
                            }
                        }
                    }
                    nestedTuples.addSize(index, objects.size());
                } else {
                    nestedTuples.addSize(index, 0);
                }

                index++;
            }

            // Apply the tuple transformator on a multiset of the nested sets
            tupleTransformator.transformAll(nestedTuples);

            // Build views and add them to collections
            UpdatableViewMap updatableViewMap = new UpdatableViewMap();
            tupleListIter = tuples.iterator();
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object collection = createCollection();
                if (tuple[startIndex] != null) {
                    List<Object[]> objects = (List<Object[]>) tuple[startIndex];
                    for (int i = 0; i < objects.size(); i++) {
                        Object[] transformedTuple = subviewTupleTransformer.transform(objects.get(i), updatableViewMap);
                        add(collection, transformedTuple[0]);
                    }
                }
                tuple[startIndex] = collection;
            }
        } else {
            // First, go through all multi-sets and convert the fields
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                if (tuple[startIndex] != null) {
                    List<Object[]> objects = (List<Object[]>) tuple[startIndex];
                    for (int i = 0; i < objects.size(); i++) {
                        Object[] elementTuple = objects.get(i);
                        for (int j = 0; j < fieldConverters.length; j++) {
                            if (elementTuple[j] instanceof CharSequence) {
                                elementTuple[j] = fieldConverters[j].fromString((CharSequence) elementTuple[j]);
                            }
                        }
                    }
                }
            }

            // Build views and add them to collections
            UpdatableViewMap updatableViewMap = new UpdatableViewMap();
            tupleListIter = tuples.iterator();
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object collection = createCollection();
                if (tuple[startIndex] != null) {
                    List<Object[]> objects = (List<Object[]>) tuple[startIndex];
                    // Before building the subviews, apply the tuple transformator on the nested set in isolation
                    tupleTransformator.transformAll(objects);
                    for (int i = 0; i < objects.size(); i++) {
                        Object[] transformedTuple = subviewTupleTransformer.transform(objects.get(i), updatableViewMap);
                        add(collection, transformedTuple[0]);
                    }
                }
                tuple[startIndex] = collection;
            }
        }

        return tuples;
    }

    protected abstract Object createCollection();

    @SuppressWarnings("unchecked")
    protected void add(Object collection, Object value) {
        if (elementConverter != null) {
            value = elementConverter.convertToViewType(value);
        }
        if (value != null) {
            addToCollection((C) collection, value);
        }
    }

    protected abstract void addToCollection(C set, Object value);

}
