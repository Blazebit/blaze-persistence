/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.cte.model;

import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.With;
import com.blazebit.persistence.view.testsuite.cte.model.PersonWithPartnerDocumentFullAged.FullAgedCTEProvider;

import java.util.Map;

@With(FullAgedCTEProvider.class)
@EntityView(Person.class)
@EntityViewInheritanceMapping("age >= 18")
public interface PersonWithPartnerDocumentFullAged extends PersonWithPartnerDocument {

    class FullAgedCTEProvider implements CTEProvider {

        @Override
        public void applyCtes(CTEBuilder<?> builder, Map<String, Object> optionalParameters) {
        }

    }

}
