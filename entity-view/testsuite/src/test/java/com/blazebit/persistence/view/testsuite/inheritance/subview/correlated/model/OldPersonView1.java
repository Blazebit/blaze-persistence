/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Person.class)
@EntityViewInheritanceMapping("age > 15")
public interface OldPersonView1 extends PersonBaseView1 {

    @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "CONCAT('', id)", correlationExpression = "CONCAT('', id) = correlationKey", correlationResult = "CONCAT('Old ', name)", fetch = FetchStrategy.JOIN)
    public String getName();
}
