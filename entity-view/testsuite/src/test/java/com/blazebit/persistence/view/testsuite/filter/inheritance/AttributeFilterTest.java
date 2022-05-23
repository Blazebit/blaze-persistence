/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.filter.inheritance;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.filter.inheritance.model.AttributeFilterInheritancePrimitiveDocumentView;
import com.blazebit.persistence.view.testsuite.filter.inheritance.model.AttributeFilterInheritancePrimitiveDocumentViewSub1;
import com.blazebit.persistence.view.testsuite.filter.inheritance.model.AttributeFilterInheritancePrimitiveDocumentViewSub2;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.3.0
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
    // DataNucleus apparently thinks NULL has a specific type which isn't the one of other result arms of a CASE WHEN clause
    @Category({ NoDatanucleus.class })
    public void testAttributeFilterWithInheritance() {
        EntityViewManager evm = build(
                AttributeFilterInheritancePrimitiveDocumentView.class,
                AttributeFilterInheritancePrimitiveDocumentViewSub1.class,
                AttributeFilterInheritancePrimitiveDocumentViewSub2.class
        );

        EntityViewSetting<AttributeFilterInheritancePrimitiveDocumentView, CriteriaBuilder<AttributeFilterInheritancePrimitiveDocumentView>> setting = EntityViewSetting.create(AttributeFilterInheritancePrimitiveDocumentView.class);
        setting.addAttributeFilter("name", "JACK");

        CriteriaBuilder<AttributeFilterInheritancePrimitiveDocumentView> cb = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class));
        String caseExpression = "CASE WHEN primitiveDocument.name = 'doc1' THEN owner_1.name WHEN primitiveDocument.name = 'doc2' THEN UPPER(owner_1.name) ELSE NULL END";
        assertEquals("SELECT CASE WHEN primitiveDocument.name = 'doc1' THEN 1 WHEN primitiveDocument.name = 'doc2' THEN 2 ELSE 0 END AS AttributeFilterInheritancePrimitiveDocumentView_class," +
                " primitiveDocument.id AS AttributeFilterInheritancePrimitiveDocumentView_id, " + caseExpression + " AS AttributeFilterInheritancePrimitiveDocumentView_name" +
                " FROM PrimitiveDocument primitiveDocument" +
                " LEFT JOIN primitiveDocument.owner owner_1" +
                " WHERE " + caseExpression + " = :param_0", cb.getQueryString());
        List<AttributeFilterInheritancePrimitiveDocumentView> list = cb.getResultList();
        assertEquals(1, list.size());
    }

}
