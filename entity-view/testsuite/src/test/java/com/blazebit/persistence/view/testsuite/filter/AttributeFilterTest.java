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

package com.blazebit.persistence.view.testsuite.filter;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.filter.model.AttributeFilterNameClashView;
import com.blazebit.persistence.view.testsuite.filter.model.AttributeFilterPrimitiveDocumentView;
import com.blazebit.persistence.view.testsuite.filter.model.MultipleDefaultAttributeFiltersView;
import com.blazebit.persistence.view.testsuite.filter.model.ViewFilterPrimitiveDocumentView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class AttributeFilterTest extends AbstractEntityViewTest {

    private PrimitiveDocument doc1;
    private PrimitiveDocument doc2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                PrimitiveDocument.class,
                PrimitivePerson.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new PrimitiveDocument("doc1");
                doc2 = new PrimitiveDocument("doc2");

                PrimitivePerson o1 = new PrimitivePerson("James");
                PrimitivePerson o2 = new PrimitivePerson("Jack");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, PrimitiveDocument.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, PrimitiveDocument.class).where("name").eq("doc2").getSingleResult();
    }

    @Test
    public void testMultipleDefaultFilters() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(MultipleDefaultAttributeFiltersView.class);
        verifyException(cfg, IllegalArgumentException.class).createEntityViewManager(cbf);
        assertMessageContains("Illegal duplicate filter name mapping ''", caughtException());
    }

    @Test
    public void testFilterNameClash() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(AttributeFilterNameClashView.class);
        verifyException(cfg, IllegalArgumentException.class).createEntityViewManager(cbf);
        assertMessageContains("Illegal duplicate filter name mapping 'filter'", caughtException());
    }

    @Test
    public void testMultipleActiveFilterPerAttribute() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(AttributeFilterPrimitiveDocumentView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        EntityViewSetting<AttributeFilterPrimitiveDocumentView, CriteriaBuilder<AttributeFilterPrimitiveDocumentView>> setting = EntityViewSetting.create(AttributeFilterPrimitiveDocumentView.class);
        setting.addAttributeFilter("name", "pete");
        IllegalArgumentException caughtException;
        try {
            setting.addAttributeFilter("name", "caseSensitiveNameFilter", "pete");
            Assert.fail("Expected " + IllegalArgumentException.class.getName());
            caughtException = null;
        } catch (IllegalArgumentException e) {
            caughtException = e;
        }
        assertMessageContains("At most one active attribute filter per attribute is allowed!", caughtException);
    }

    @Test
    public void testOverwriteDefaultAttributeFilterValue() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(AttributeFilterPrimitiveDocumentView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        EntityViewSetting<AttributeFilterPrimitiveDocumentView, CriteriaBuilder<AttributeFilterPrimitiveDocumentView>> setting = EntityViewSetting.create(AttributeFilterPrimitiveDocumentView.class);
        setting.addAttributeFilter("name", "pete");
        setting.addAttributeFilter("name", "pete2");

        assertEquals("pete2", setting.getAttributeFilters().get("name").getFilterValue());
    }

    @Test
    public void testOverwriteAttributeFilterValue() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(AttributeFilterPrimitiveDocumentView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        EntityViewSetting<AttributeFilterPrimitiveDocumentView, CriteriaBuilder<AttributeFilterPrimitiveDocumentView>> setting = EntityViewSetting.create(AttributeFilterPrimitiveDocumentView.class);
        setting.addAttributeFilter("name", "caseSensitiveNameFilter", "pete");
        setting.addAttributeFilter("name", "caseSensitiveNameFilter", "pete2");

        assertEquals("pete2", setting.getAttributeFilters().get("name").getFilterValue());
    }

    @Test
    public void testViewFilter() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(ViewFilterPrimitiveDocumentView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        EntityViewSetting<ViewFilterPrimitiveDocumentView, CriteriaBuilder<ViewFilterPrimitiveDocumentView>> setting = EntityViewSetting.create(ViewFilterPrimitiveDocumentView.class);
        setting.addViewFilter("viewFilter1");
        List<ViewFilterPrimitiveDocumentView> results = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class)).getResultList();
        assertEquals(1, results.size());
        assertEquals(Long.valueOf(doc1.getId()), results.get(0).getId());
    }

    @Test
    public void testMultipleViewFilter() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(ViewFilterPrimitiveDocumentView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        EntityViewSetting<ViewFilterPrimitiveDocumentView, CriteriaBuilder<ViewFilterPrimitiveDocumentView>> setting = EntityViewSetting.create(ViewFilterPrimitiveDocumentView.class);
        setting.addViewFilter("viewFilter1");
        setting.addViewFilter("viewFilter2");
        List<ViewFilterPrimitiveDocumentView> results = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class)).getResultList();
        assertEquals(0, results.size());
    }

    private void assertMessageContains(String expectedContainedMessage, Exception e) {
        assertTrue(e.getMessage().contains(expectedContainedMessage));
    }
}
