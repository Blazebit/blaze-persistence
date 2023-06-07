/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite.collections.index;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.index.model.DocumentViewWithMappingIndex;
import com.blazebit.persistence.view.testsuite.collections.index.model.DocumentViewWithMappingIndexJoin;
import com.blazebit.persistence.view.testsuite.collections.index.model.DocumentViewWithMappingIndexMultiset;
import com.blazebit.persistence.view.testsuite.collections.index.model.DocumentViewWithMappingIndexSelect;
import com.blazebit.persistence.view.testsuite.collections.index.model.DocumentViewWithMappingIndexSubselect;
import com.blazebit.persistence.view.testsuite.collections.index.model.VersionKeyView;
import com.blazebit.persistence.view.testsuite.collections.index.model.VersionStaticKeyView;
import com.blazebit.persistence.view.testsuite.collections.index.model.VersionViewWithMappingIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MappingIndexTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("p");
                doc1 = new Document("doc1", p, new Version(2), new Version(1));
                doc2 = new Document("doc2", p);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
    }

    @Test
    public void testJoin() {
        test(DocumentViewWithMappingIndexJoin.class);
    }

    @Test
    public void testSelect() {
        test(DocumentViewWithMappingIndexSelect.class);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSelectBatch2() {
        test(DocumentViewWithMappingIndexSelect.class, 2);
    }

    @Test
    public void testSubselect() {
        test(DocumentViewWithMappingIndexSubselect.class);
    }

    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void testMultiset() {
        test(DocumentViewWithMappingIndexMultiset.class);
    }

    private <T extends DocumentViewWithMappingIndex> void test(Class<T> clazz) {
        test(clazz, null);
    }

    private <T extends DocumentViewWithMappingIndex> void test(Class<T> clazz, Integer batchSize) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.setProperty(ConfigurationProperties.PROXY_EAGER_LOADING, "true");
        cfg.setProperty(ConfigurationProperties.UPDATER_EAGER_LOADING, "true");
        build(cfg, clazz, VersionViewWithMappingIndex.class, VersionKeyView.class, VersionStaticKeyView.class);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(clazz);
        if (batchSize != null) {
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".versions", batchSize);
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".versionMap", batchSize);
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".versionMap2", batchSize);
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".multiVersions", batchSize);
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".multiVersionMap", batchSize);
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".multiVersionMap2", batchSize);
        }
        CriteriaBuilder<T> cb = evm.applySetting(setting, criteria);
        List<T> results = cb.getResultList();

        List<Version> versions = new ArrayList<>(doc1.getVersions());
        versions.sort(Comparator.comparing(Version::getVersionIdx));
        List<Version> versionsIdSorted = new ArrayList<>(doc1.getVersions());
        versionsIdSorted.sort(Comparator.comparing(Version::getId));

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getName());
        assertEquals(2, results.get(0).getVersions().size());
        assertEquals(2, results.get(0).getVersionMap().size());
        assertEquals(2, results.get(0).getVersionMap2().size());
        assertEquals(1, results.get(0).getMultiVersions().size());
        assertEquals(1, results.get(0).getMultiVersionMap().size());
        assertEquals(1, results.get(0).getMultiVersionMap2().size());
        assertEquals(versions.get(0).getId(), results.get(0).getVersions().get(0).getId());
        assertEquals(versions.get(1).getId(), results.get(0).getVersions().get(1).getId());
        assertEquals(versions.get(0).getId(), results.get(0).getVersionMap().get(1).getId());
        assertEquals(versions.get(1).getId(), results.get(0).getVersionMap().get(2).getId());
        assertEquals(versions.get(0).getId(), results.get(0).getVersionMap2().get(VersionKeyView.of(1)).getId());
        assertEquals(versions.get(1).getId(), results.get(0).getVersionMap2().get(VersionKeyView.of(2)).getId());

        assertEquals(versionsIdSorted.get(0).getId(), results.get(0).getMultiVersions().get(0).first().getId());
        assertEquals(versionsIdSorted.get(1).getId(), results.get(0).getMultiVersions().get(0).last().getId());
        assertEquals(versionsIdSorted.get(0).getId(), results.get(0).getMultiVersionMap().get(0).first().getId());
        assertEquals(versionsIdSorted.get(1).getId(), results.get(0).getMultiVersionMap().get(0).last().getId());
        assertEquals(versionsIdSorted.get(0).getId(), results.get(0).getMultiVersionMap2().get(VersionStaticKeyView.of(0)).first().getId());
        assertEquals(versionsIdSorted.get(1).getId(), results.get(0).getMultiVersionMap2().get(VersionStaticKeyView.of(0)).last().getId());

        assertEquals("doc2", results.get(1).getName());
        assertEquals(0, results.get(1).getVersions().size());
        assertEquals(0, results.get(1).getVersionMap().size());
        assertEquals(0, results.get(1).getVersionMap2().size());
        assertEquals(0, results.get(1).getMultiVersions().size());
        assertEquals(0, results.get(1).getMultiVersionMap().size());
        assertEquals(0, results.get(1).getMultiVersionMap2().size());
    }

}
