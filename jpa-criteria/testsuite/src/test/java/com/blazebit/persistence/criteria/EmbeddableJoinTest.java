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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityContainer;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable_;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity_;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.NameObject_;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.criteria.JoinType;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
// NOTE: See EmbeddableComplexTest for details about why we need to ignore DataNucleus and EclipseLink
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class EmbeddableJoinTest extends AbstractCoreTest {

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
    public void testJoinEmbeddableNested() {
        BlazeCriteriaQuery<String> cq = BlazeCriteria.get(cbf, String.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<EmbeddableTestEntity> root = cq.from(EmbeddableTestEntity.class, "e");
        BlazeJoin<EmbeddableTestEntity, EmbeddableTestEntityEmbeddable> p1 = root.join(EmbeddableTestEntity_.embeddable, "e2", JoinType.INNER);
        BlazeJoin<EmbeddableTestEntityEmbeddable, NameObject> p2 = p1.join(EmbeddableTestEntityEmbeddable_.nameObject, "e3", JoinType.LEFT);

        cq.select(p2.get(NameObject_.primaryName));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT e.embeddable.nameObject.primaryName FROM EmbeddableTestEntity e", criteriaBuilder.getQueryString());
    }

    @Test
    public void testJoinEmbeddableNestedWithoutAliases() {
        BlazeCriteriaQuery<String> cq = BlazeCriteria.get(cbf, String.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<EmbeddableTestEntity> root = cq.from(EmbeddableTestEntity.class);
        BlazeJoin<EmbeddableTestEntity, EmbeddableTestEntityEmbeddable> p1 = root.join(EmbeddableTestEntity_.embeddable, JoinType.INNER);
        BlazeJoin<EmbeddableTestEntityEmbeddable, NameObject> p2 = p1.join(EmbeddableTestEntityEmbeddable_.nameObject, JoinType.LEFT);

        cq.select(p2.get(NameObject_.primaryName));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT generatedEmbeddableTestEntity_0.embeddable.nameObject.primaryName FROM EmbeddableTestEntity generatedEmbeddableTestEntity_0", criteriaBuilder.getQueryString());
    }

}
