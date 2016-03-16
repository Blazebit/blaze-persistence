/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.view.testsuite.update.model;

import java.util.List;

import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface UpdatableDocumentWithCollectionsView {
    
    @IdMapping("id")
    public Long getId();

    public String getName();

    public void setName(String name);

	public List<Person> getPersonList();
	
	public void setPersonList(List<Person> personList);

}
