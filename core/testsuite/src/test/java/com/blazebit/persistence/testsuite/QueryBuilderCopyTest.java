/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentNodeCTE;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class QueryBuilderCopyTest extends AbstractCoreTest {

    @Test
    // Test for issue #602
    public void testQueryCopying() {
        // The key to reproducing the bug is having the parameter in the select clause multiple times
        cbf.create(em, Long.class)
                .from(Document.class)
                .select("COALESCE(id,:param3)")
                .select("COALESCE(id,:param3)")
                .copy(String.class)
                .select("name");
    }

    @Test
    public void testQueryCopyingCorrelated() {
        cbf.create(em, String.class)
                .from(Document.class, "doc")
                .select("doc.owner.localized")
                .innerJoinOn(Person.class, "o")
                    .on("o.friend.localized").eq("pers1")
                .end()
                .copy(String.class)
                .select("name")
                .getResultList();
    }

    @Test
    @Category({ NoEclipselink.class, NoOracle.class})
    public void testQueryCopyingWithCte() {
        cbf.create(em, String.class)
            .from(Document.class, "doc")
            .select("doc.owner.localized")
            .innerJoinOn(Person.class, "o")
                .on("o.friend.localized").eq("pers1")
            .end()
            .innerJoinOnSubquery(DocumentNodeCTE.class, "dn")
                .from(Document.class, "dd")
                    .bind("id").select("dd.id", "id")
                    .bind("parentId").select("dd.parent.id", "parentId")
                .end()
                .on("doc.id").eqExpression("dn.id")
            .end()
            .copy(String.class)
            .select("doc.name")
            .getResultList();
    }
}
