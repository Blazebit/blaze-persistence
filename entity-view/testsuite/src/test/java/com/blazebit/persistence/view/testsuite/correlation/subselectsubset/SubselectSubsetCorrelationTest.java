/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.DocumentEmbeddingViewSubselectWithoutIdView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.DocumentSubselectElementCollectionView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.NameObjectView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.PersonSubselectView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.SimplePersonSubView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SubselectSubsetCorrelationTest extends AbstractCorrelationTest {

    @Test
    // MySQL doesn't support limit in subquery of IN predicate
    @Category({ NoMySQL.class })
    public void testSubselectSubsetCorrelation() {
        EntityViewManager evm = build(
                PersonSubselectView.class,
                SimplePersonSubView.class
        );

        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
                .orderByDesc("name")
                .orderByDesc("id")
                .setMaxResults(2);
        EntityViewSetting<PersonSubselectView, CriteriaBuilder<PersonSubselectView>> setting =
                EntityViewSetting.create(PersonSubselectView.class);
        CriteriaBuilder<PersonSubselectView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

    @Test
    public void testSubselectElementCollection() {
        EntityViewManager evm = build(
                DocumentSubselectElementCollectionView.class,
                NameObjectView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .where("name").eq("doc1");
        EntityViewSetting<DocumentSubselectElementCollectionView, CriteriaBuilder<DocumentSubselectElementCollectionView>> setting =
                EntityViewSetting.create(DocumentSubselectElementCollectionView.class);
        CriteriaBuilder<DocumentSubselectElementCollectionView> cb = evm.applySetting(setting, criteria);
        List<DocumentSubselectElementCollectionView> resultList = cb.getResultList();
        Assert.assertTrue(resultList.get(0).getNames().isEmpty());
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testPaginatedSubselectElementCollectionWithFilter() {
        EntityViewManager evm = build(
            DocumentSubselectElementCollectionView.class,
            NameObjectView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        EntityViewSetting<DocumentSubselectElementCollectionView, PaginatedCriteriaBuilder<DocumentSubselectElementCollectionView>> setting =
            EntityViewSetting.create(DocumentSubselectElementCollectionView.class, 0, 1);
        setting.addAttributeFilter("name", "doc1");
        PaginatedCriteriaBuilder<DocumentSubselectElementCollectionView> cb = evm.applySetting(setting, criteria);
        List<DocumentSubselectElementCollectionView> resultList = cb.getResultList();
        Assert.assertTrue(resultList.get(0).getNames().isEmpty());
    }

    @Test
    public void testSubselectWithEmbeddingViewWithoutIdMapping() {
        try {
            build(
                    DocumentEmbeddingViewSubselectWithoutIdView.class,
                    SimplePersonSubView.class
            );
            Assert.fail("Expected to fail because of missing @IdMapping which is required for the use of EMBEDDING_VIEW in SUBSELECT and SELECT correlations");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("does not declare a @IdMapping"));
        }
    }

}
