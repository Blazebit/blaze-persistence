/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.type.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;

import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentSubqueryTypeConverterView extends Serializable {

    @IdMapping
    public Long getId();

    @MappingSubquery(value = SubqueryCorrelator.class, subqueryAlias = "q", expression = "COALESCE(q, 0L)")
    public String getAge();

    public static class SubqueryCorrelator implements SubqueryProvider {
        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            return subqueryInitiator.from(Document.class)
                    .select("age")
                    .where("EMBEDDING_VIEW(id)").eqExpression("id")
                    .end();
        }
    }

}
