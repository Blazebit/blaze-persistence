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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinOnTest extends AbstractCoreTest {

    @Test
    public void testLeftJoinOn() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.leftJoinOn("d.partners.localized", "l").on("l").like("%dld").end();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners LEFT JOIN partners.localized l WITH l LIKE :param_0", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testRightJoinOn() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.rightJoinOn("d.partners.localized", "l").on("l").like("%dld").end();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners RIGHT JOIN partners.localized l WITH l LIKE :param_0", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testInnerJoinOn() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.innerJoinOn("d.partners.localized", "l").on("l").like("%dld").end();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners JOIN partners.localized l WITH l LIKE :param_0", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeftJoinOnComplex() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.leftJoinOn("d.partners.localized", "l").on("l").like("%dld")
                .on("l").gt("1")
                .onOr()
                .on("l").eq("2")
                .onAnd()
                .on("l").eq("3")
                .onOr()
                .on("l").eq("4")
                .endOr()
                .endAnd()
                .endOr().end();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners LEFT JOIN partners.localized l WITH l LIKE :param_0 AND l > :param_1 AND (l = :param_2 OR (l = :param_3 AND (l = :param_4)))", crit.getQueryString());
        crit.getResultList();
    }
}
