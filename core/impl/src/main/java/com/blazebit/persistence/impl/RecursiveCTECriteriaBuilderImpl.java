package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.RecursiveCTECriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

public class RecursiveCTECriteriaBuilderImpl<T, X> extends AbstractCTECriteriaBuilder<T, X, RecursiveCTECriteriaBuilder<T, X>> implements RecursiveCTECriteriaBuilder<T, X>, CTEBuilderListener {

	protected boolean done;
	protected CTECriteriaBuilderImpl<T, X> recursiveCteBuilder;

	public RecursiveCTECriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, CriteriaBuilder<X> result, final CTEBuilderListener listener) {
		super(cbf, em, dbmsDialect, clazz, registeredFunctions, result, listener);
	}

	@Override
	public CTECriteriaBuilder<T, X> unionAll() {
		verifyBuilderEnded();
		recursiveCteBuilder = new CTECriteriaBuilderImpl<T, X>(cbf, em, dbmsDialect, resultType, registeredFunctions, result, this);
		return recursiveCteBuilder;
	}

	@Override
	public void onBuilderStarted(AbstractCTECriteriaBuilder<?, ?, ?> builder) {
		// Don't care about that
	}

	@Override
	public void onBuilderEnded(AbstractCTECriteriaBuilder<?, ?, ?> builder) {
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
		List<String> attributes = new ArrayList<String>(bindingMap.size());
		List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
		List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
		newSelectInfos.clear();
		
		for (Map.Entry<String, Integer> bindingEntry : bindingMap.entrySet()) {
			Integer newPosition = attributes.size();
			attributes.add(bindingEntry.getKey());
			newSelectInfos.add(originalSelectInfos.get(bindingEntry.getValue()));
			bindingEntry.setValue(newPosition);
		}
		
		// As a side effect, this will reorder selects according to attribute order
		recursiveCteBuilder.createCTEInfo();
		CTEInfo info = new CTEInfo(cteName, attributes, true, this, recursiveCteBuilder);
		return info;
	}

}
