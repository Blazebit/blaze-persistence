/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.testsuite.basic.model;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(name = "PersView", value = Person.class)
public interface PersonViewWithSingularMapping extends IdHolderView<Long> {

    @MappingSingular
    public Set<Document> getOwnedDocuments();
}
