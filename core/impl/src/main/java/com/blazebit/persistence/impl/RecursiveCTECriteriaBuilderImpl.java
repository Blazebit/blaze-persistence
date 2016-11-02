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
        
        // As a side effect, this will reorder selects according to attribute order
        recursiveCteBuilder.createCTEInfo();
        CTEInfo info = new CTEInfo(cteName, cteType, attributes, true, unionAll, this, recursiveCteBuilder);
        return info;
    }

}
