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

package com.blazebit.persistence.view.testsuite.update.subview.nested.creatable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public abstract class UpdatableDocumentView {
    
    @IdMapping
    public abstract Long getId();

    public abstract Long getVersion();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Date getLastModified();
    
    public abstract void setLastModified(Date date);

    public abstract UpdatableResponsiblePersonView getResponsiblePerson();

    public abstract void setResponsiblePerson(UpdatableResponsiblePersonView responsiblePerson);

}
