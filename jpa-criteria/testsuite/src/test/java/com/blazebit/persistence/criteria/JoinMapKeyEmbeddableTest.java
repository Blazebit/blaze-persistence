/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps_;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityContainer;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable_;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity_;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps_;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
// NOTE: EclipseLink doesn't support Map in embeddables: https://bugs.eclipse.org/bugs/show_bug.cgi?id=391062
// TODO: report that datanucleus doesn't support element collection in an embeddable
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class JoinMapKeyEmbeddableTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IntIdEntity.class,
                EmbeddableTestEntity.class,
                EmbeddableTestEntitySub.class,
                EmbeddableTestEntityContainer.class,
                EmbeddableTestEntityEmbeddable.class,
                NameObject.class,
                EmbeddableTestEntityNestedEmbeddable.class
        };
    }

    @Test
    public void joinEmbeddableMapKey() {
        BlazeCriteriaQuery<String> cq = BlazeCriteria.get(cbf, String.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<EmbeddableTestEntity> root = cq.from(EmbeddableTestEntity.class, "e");
        BlazeMapJoin<EmbeddableTestEntityEmbeddable, String, IntIdEntity> join = root.join(EmbeddableTestEntity_.embeddable).join(EmbeddableTestEntityEmbeddable_.manyToMany, "map");

        join.on(cb.equal(join.key(), "abc"));
        cq.select(join.key());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT KEY(map) FROM EmbeddableTestEntity e JOIN e.embeddable.manyToMany map" + onClause("KEY(map) = :generated_param_0"), criteriaBuilder.getQueryString());
        criteriaBuilder.getResultList();
    }

}
