/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CountSubqueryProvider implements SubqueryProvider {

    @Override
    public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
        return subqueryBuilder.from(Person.class, "personSub")
            .where("partnerDocument.id").eqExpression("OUTER(id)")
            .select("COUNT(personSub.id)")
            .end();
    }

}
