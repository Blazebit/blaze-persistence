/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.entity.creatableonly.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.entity.model.UpdatableDocumentEntityViewBase;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentEntityView extends UpdatableDocumentEntityViewBase {

    @UpdatableMapping(updatable = false, cascade = { CascadeType.PERSIST })
    public Person getResponsiblePerson();
    
    public void setResponsiblePerson(Person responsiblePerson);

}
