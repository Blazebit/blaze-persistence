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

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.ContactsDocumentView;
import com.blazebit.persistence.view.testsuite.basic.model.SimplePersonView;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.entity.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public class DuplicateResultsTest extends AbstractEntityViewTest {

    private EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SimplePersonView.class);
        cfg.addEntityView(ContactsDocumentView.class);
        evm = cfg.createEntityViewManager(cbf, em.getEntityManagerFactory());
    }

    @Test
    public void testDuplicateResults() {
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");

        Document d1 = new Document();
        d1.setOwner(p1);

        em.persist(p1);
        em.persist(p2);
        em.persist(d1);

        p1.setPartnerDocument(d1);

        em.flush();
        em.clear();

        EntityViewSetting<SimplePersonView, CriteriaBuilder<SimplePersonView>> setting = EntityViewSetting.create(SimplePersonView.class);
        setting.addOptionalParameter("contactPersonNumber", 1);
        List<SimplePersonView> versions = evm.applySetting(setting, cbf.create(em, Person.class))
                .getResultList();

        assertEquals(1, versions.size());
    }

}
