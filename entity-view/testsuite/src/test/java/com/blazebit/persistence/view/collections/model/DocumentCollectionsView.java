/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.collections.model;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cpbec
 */
@EntityView(Document.class)
public interface DocumentCollectionsView {
    
    public String getName();
    
    @Mapping("contacts2")
    public Map<Integer, Person> getContacts();
    
    @Mapping("partners")
    public Set<Person> getPartners();
    
    public List<Person> getPersonList();
}
