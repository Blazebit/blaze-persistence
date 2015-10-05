package com.blazebit.persistence.impl;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

public class RecursiveCTECriteriaBuilderImpl<T, Y, X> extends AbstractCTECriteriaBuilder<T, Y, SelectRecursiveCTECriteriaBuilder<T, Y>> implements SelectRecursiveCTECriteriaBuilder<T, Y>, CTEBuilderListener {

    protected final Class<T> clazz;
	protected boolean done;
	protected CTECriteriaBuilderImpl<T, Y, X> recursiveCteBuilder;

	public RecursiveCTECriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, Y result, final CTEBuilderListener listener) {
		super(cbf, em, dbmsDialect, clazz, registeredFunctions, result, listener);
		this.clazz = clazz;
	}

	@Override
	public SelectCTECriteriaBuilder<T, Y> unionAll() {
		verifyBuilderEnded();
		recursiveCteBuilder = new CTECriteriaBuilderImpl<T, Y, X>(cbf, em, dbmsDialect, clazz, registeredFunctions, result, this);
		return recursiveCteBuilder;
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
		CTEInfo info = new CTEInfo(cteName, cteType, attributes, true, this, recursiveCteBuilder);
		return info;
	}

}
