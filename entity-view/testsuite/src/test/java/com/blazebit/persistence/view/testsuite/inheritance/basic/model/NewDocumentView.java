/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.testsuite.entity.Document;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
@EntityViewInheritance({ NewSub1DocumentView.class, NewSub2DocumentView.class })
@EntityViewInheritanceMapping("age < 15")
public interface NewDocumentView extends DocumentBaseView {
    
    public Set<SimplePersonSubView> getContacts();
}
