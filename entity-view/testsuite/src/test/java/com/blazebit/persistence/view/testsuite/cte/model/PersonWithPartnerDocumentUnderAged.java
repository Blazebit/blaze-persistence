/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.cte.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView;

@EntityView(Person.class)
@EntityViewInheritanceMapping("age < 18")
public interface PersonWithPartnerDocumentUnderAged extends PersonWithPartnerDocument {

}
