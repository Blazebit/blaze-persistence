/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.JuniorProjectLeader;
import com.blazebit.persistence.testsuite.entity.LargeProject;
import com.blazebit.persistence.testsuite.entity.Project;
import com.blazebit.persistence.testsuite.entity.ProjectLeader;
import com.blazebit.persistence.testsuite.entity.SeniorProjectLeader;
import com.blazebit.persistence.testsuite.entity.SmallProject;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class InheritanceTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            ProjectLeader.class,
            JuniorProjectLeader.class,
            SeniorProjectLeader.class,
            Project.class,
            SmallProject.class,
            LargeProject.class
        };
    }
    
    @Test
    // NOTE: There is a bug in the metamodel generation when used in eclipse: HHH-10265
    public void testInheritanceWithEntityName() {
        @SuppressWarnings("rawtypes")
        CriteriaBuilder<Project> cb = cbf.create(em, Project.class, "p");
        String expectedQuery = "SELECT p FROM Projects p";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    // NOTE: There is a bug in the metamodel generation when used in eclipse: HHH-10265
    public void testJoinPolymorphicEntity() {
        @SuppressWarnings("rawtypes")
        CriteriaBuilder<Project> cb = cbf.create(em, Project.class, "p")
                .leftJoinFetch("leader", "l");
        String expectedQuery = "SELECT p FROM Projects p LEFT JOIN FETCH p.leader l";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    // NOTE: There is a bug in the metamodel generation when used in eclipse: HHH-10265
    public void testImplicitJoinPolymorphicEntity() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Project.class, "p")
                .select("leader.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("p.leader.id", "leader_1") + " FROM Projects p" + singleValuedAssociationIdJoin("p.leader", "leader_1", true);
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
