/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subquery.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class TestEmbeddingViewSubqueryProvider implements SubqueryProvider {

    @Override
    public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
        return subqueryBuilder.from(Person.class)
            .where("partnerDocument.id").eqExpression("EMBEDDING_VIEW(id)")
            .select("COUNT(id)")
            .end();
    }

}
