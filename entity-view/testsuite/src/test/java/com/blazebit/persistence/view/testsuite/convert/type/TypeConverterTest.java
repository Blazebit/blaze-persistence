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

package com.blazebit.persistence.view.testsuite.convert.type;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.TypeConverter;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.convert.type.model.DocumentTypeConverterView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TypeConverterTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    private Document doc;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc = new Document("doc1", 1);
                Person pers1 = new Person("pers1");

                em.persist(pers1);

                doc.setOwner(pers1);
                em.persist(doc);
            }
        });
    }

    @Test
    public void testWithoutTypeConverter() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentTypeConverterView.class);
        try {
            cfg.createEntityViewManager(cbf);
            Assert.fail("Building EntityViewManager should fail because of a type conflict!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("The resolved possible types [long] are not assignable to the given expression type 'java.lang.String'"));
        }
    }

    @Test
    public void testTypeConverter() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentTypeConverterView.class);
        cfg.registerTypeConverter(long.class, String.class, new TypeConverter<Long, String>() {
            @Override
            public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
                return String.class;
            }

            @Override
            public String convertToViewType(Long object) {
                return Long.toString(object);
            }

            @Override
            public Long convertToUnderlyingType(String object) {
                return Long.valueOf(object);
            }
        });
        evm = cfg.createEntityViewManager(cbf);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        DocumentTypeConverterView documentView = evm.applySetting(EntityViewSetting.create(DocumentTypeConverterView.class), criteria)
                .getSingleResult();

        assertEquals("1", documentView.getAge());
    }

    @Test
    public void testTypeConverterBoxed() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentTypeConverterView.class);
        cfg.registerTypeConverter(Long.class, String.class, new TypeConverter<Long, String>() {
            @Override
            public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
                return String.class;
            }

            @Override
            public String convertToViewType(Long object) {
                return Long.toString(object);
            }

            @Override
            public Long convertToUnderlyingType(String object) {
                return Long.valueOf(object);
            }
        });
        evm = cfg.createEntityViewManager(cbf);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        DocumentTypeConverterView documentView = evm.applySetting(EntityViewSetting.create(DocumentTypeConverterView.class), criteria)
                .getSingleResult();

        assertEquals("1", documentView.getAge());
    }
}
