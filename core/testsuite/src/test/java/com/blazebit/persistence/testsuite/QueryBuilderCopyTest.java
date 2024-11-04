/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;

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
}
