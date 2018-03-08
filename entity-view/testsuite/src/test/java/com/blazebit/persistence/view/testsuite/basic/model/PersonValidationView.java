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

import java.util.Calendar;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonValidationView extends IdHolderView<Long> {
    
    // map collection target entity property to collection of property type 
    @Mapping("ownedDocuments.id")
    public Set<Long> getOwnedDocumentIds();

    // The case when a collection is mapped, but we are sure there is only one element
    @Mapping("partnerDocument.versions.url")
    public String getVersionUrl();
    
    @Mapping("FUNCTION('TIMESTAMP_ADD_DAY', partnerDocument.creationDate, -age)")
    public Calendar getDateComputation();
    
    @Mapping("CONCAT(COALESCE(name,'-'),' ',COALESCE(partnerDocument.name,'-'))")
    public abstract String getCoalescingConcat();
    
    // Parameters are totally ok
    @Mapping("COALESCE(partnerDocument.contacts[:firstContact].name, partnerDocument.contacts[partnerDocument.defaultContact].name)")
    public String getName();
    
    @Mapping("CASE WHEN age = 9 THEN 0 ELSE 1 END")
    public abstract Integer getNestedCaseWhen();
}
