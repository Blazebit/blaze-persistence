/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.builder.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Document.class)
public abstract class DocumentBuilderView {

    private final Object test;

    public DocumentBuilderView(@MappingParameter("test") Object test) {
        this.test = test;
    }

    public Object getTest() {
        return test;
    }

    @IdMapping
    public abstract Long getId();
    public abstract String getName();
    public abstract long getAge();
    public abstract PersonView getOwner();
    public abstract Map<Integer, PersonView> getContacts();
    public abstract List<PersonView> getPeople();
    public abstract List<PersonView> getPeopleListBag();
    public abstract Set<PersonView> getPartners();
    public abstract List<String> getStrings();
}
