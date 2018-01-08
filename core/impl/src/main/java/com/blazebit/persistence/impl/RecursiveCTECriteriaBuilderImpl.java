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

package com.blazebit.persistence.impl;

import java.util.List;

import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;

public class RecursiveCTECriteriaBuilderImpl<Y> extends AbstractCTECriteriaBuilder<Y, SelectRecursiveCTECriteriaBuilder<Y>, SelectCTECriteriaBuilder<Y>, Void> implements SelectRecursiveCTECriteriaBuilder<Y>, CTEBuilderListener {

    protected final Class<Object> clazz;
    protected boolean done;
    protected boolean unionAll;
    protected SelectCTECriteriaBuilderImpl<Y> recursiveCteBuilder;

    public RecursiveCTECriteriaBuilderImpl(MainQuery mainQuery, String cteName, Class<Object> clazz, Y result, final CTEBuilderListener listener) {
        super(mainQuery, cteName, clazz, result, listener, null);
        this.clazz = clazz;
    }

    @Override
    public SelectCTECriteriaBuilderImpl<Y> union() {
        verifyBuilderEnded();
        unionAll = false;
        recursiveCteBuilder = new SelectCTECriteriaBuilderImpl<Y>(mainQuery, cteName, clazz, result, this);
        return recursiveCteBuilder;
    }

    @Override
    public SelectCTECriteriaBuilderImpl<Y> unionAll() {
        verifyBuilderEnded();
        unionAll = true;
        recursiveCteBuilder = new SelectCTECriteriaBuilderImpl<Y>(mainQuery, cteName, clazz, result, this);
        return recursiveCteBuilder;
    }

    @Override
    public void onReplaceBuilder(CTEInfoBuilder oldBuilder, CTEInfoBuilder newBuilder) {
        // Don't care about that
    }

    @Override
    public void onBuilderStarted(CTEInfoBuilder builder) {
        // Don't care about that
    }

    @Override
    public void onBuilderEnded(CTEInfoBuilder builder) {
        done = true;
        listener.onBuilderEnded(this);
    }
    
    public void verifyBuilderEnded() {
        if (recursiveCteBuilder != null && !done) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    @Override
    public CTEInfo createCTEInfo() {
        verifyBuilderEnded();
        List<String> attributes = prepareAndGetAttributes();
        List<String> columns = prepareAndGetColumnNames();
        
        // As a side effect, this will reorder selects according to attribute order
        recursiveCteBuilder.createCTEInfo();
        CTEInfo info = new CTEInfo(cteName, cteType, attributes, columns, true, unionAll, this, recursiveCteBuilder);
        return info;
    }

}
