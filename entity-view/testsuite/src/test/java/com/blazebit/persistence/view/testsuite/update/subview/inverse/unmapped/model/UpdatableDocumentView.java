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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentView extends DocumentIdView {

    public String getName();
    public void setName(String name);

    @UpdatableMapping
    PersonIdView getOwner();
    void setOwner(PersonIdView owner);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    @UpdatableMapping
    Set<VersionIdView> getVersions();
    void setVersions(Set<VersionIdView> versions);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.SET_NULL)
    @UpdatableMapping
    Set<UpdatablePersonView> getPartners();
    void setPartners(Set<UpdatablePersonView> partners);
}
