/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.spring.data.base.query.AbstractCriteriaQueryParameterBinder;
import com.blazebit.persistence.spring.data.base.query.JpaParameters;
import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

/**
 * Concrete version for Spring Data 2.x.
 * 
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CriteriaQueryParameterBinder extends AbstractCriteriaQueryParameterBinder {

    public CriteriaQueryParameterBinder(EntityManager em, EntityViewManager evm, JpaParameters parameters, Object[] values, Iterable<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
        super(em, evm, parameters, values, expressions);
    }

    @Override
    protected int getOffset() {
        Pageable pageable = getPageable();
        if (pageable instanceof KeysetPageable) {
            return ((KeysetPageable) pageable).getIntOffset();
        } else {
            return (int) pageable.getOffset();
        }
    }
}
