/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
