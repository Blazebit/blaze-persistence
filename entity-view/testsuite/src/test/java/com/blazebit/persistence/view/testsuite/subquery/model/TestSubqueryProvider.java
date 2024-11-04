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
 * @since 1.0.0
 */
public class TestSubqueryProvider implements SubqueryProvider {
    
    private final Integer parameterValue;

    public TestSubqueryProvider(@MappingParameter("optionalParameter") Integer parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Override
    public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
        return subqueryBuilder.from(Person.class)
            .where("partnerDocument.id").eqExpression("OUTER(id)")
            .where("1").eqExpression("" + parameterValue)
            .select("COUNT(id)")
            .end();
    }

}
