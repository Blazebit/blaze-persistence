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
package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TupleTransformatorFactory {

    private final List<TupleTransformatorFactoryLevel> transformatorLevels = new ArrayList<TupleTransformatorFactoryLevel>();
    private int currentLevel = 0;

    public TupleTransformatorFactory() {
        transformatorLevels.add(new TupleTransformatorFactoryLevel());
    }

	public boolean hasTransformers() {
        return transformatorLevels.get(0).tupleListTransformers.size() > 0
            || transformatorLevels.get(0).tupleTransformerFactories.size() > 0;
    }

    public void add(TupleTransformatorFactory tupleTransformator) {
        if (!tupleTransformator.hasTransformers()) {
            return;
        }

        for (int i = 0; i < tupleTransformator.transformatorLevels.size(); i++) {
            if (i != 0) {
                incrementLevel();
            }

            TupleTransformatorFactoryLevel thisLevel = transformatorLevels.get(currentLevel);
            TupleTransformatorFactoryLevel otherLevel = tupleTransformator.transformatorLevels.get(i);
            thisLevel.tupleTransformerFactories.addAll(otherLevel.tupleTransformerFactories);
            thisLevel.tupleListTransformers.addAll(otherLevel.tupleListTransformers);
        }
    }

    private void incrementLevel() {
        currentLevel++;
        transformatorLevels.add(new TupleTransformatorFactoryLevel());
    }

    public void add(TupleListTransformer tupleListTransformer) {
        transformatorLevels.get(currentLevel).tupleListTransformers.add(tupleListTransformer);
        incrementLevel();
    }

    public void add(TupleTransformerFactory tupleTransformerFactory) {
        transformatorLevels.get(currentLevel).tupleTransformerFactories.add(tupleTransformerFactory);
    }

    public TupleTransformator create(QueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters) {
    	List<TupleTransformatorLevel> newTransformatorLevels = new ArrayList<TupleTransformatorLevel>(transformatorLevels.size());
        for (TupleTransformatorFactoryLevel thisLevel : transformatorLevels) {
            final List<TupleTransformer> tupleTransformers = new ArrayList<TupleTransformer>(thisLevel.tupleTransformerFactories.size());
            // No need to copy this, because TupleListTransformer are not context sensitive
            final List<TupleListTransformer> tupleListTransformers = thisLevel.tupleListTransformers;
            
            for (TupleTransformerFactory tupleTransformerFactory : thisLevel.tupleTransformerFactories) {
            	tupleTransformers.add(tupleTransformerFactory.create(queryBuilder, optionalParameters));
            }

        	newTransformatorLevels.add(new TupleTransformatorLevel(tupleTransformers, tupleListTransformers));
        }
        
        return new TupleTransformator(newTransformatorLevels);
    }
}
