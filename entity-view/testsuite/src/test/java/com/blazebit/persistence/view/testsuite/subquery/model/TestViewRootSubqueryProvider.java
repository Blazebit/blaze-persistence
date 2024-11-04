/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subquery.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TestViewRootSubqueryProvider implements SubqueryProvider {

    @Override
    public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
        return subqueryBuilder.from(Person.class)
            .where("partnerDocument.id").eqExpression("VIEW_ROOT(id)")
            .select("COUNT(id)")
            .end();
    }

}
