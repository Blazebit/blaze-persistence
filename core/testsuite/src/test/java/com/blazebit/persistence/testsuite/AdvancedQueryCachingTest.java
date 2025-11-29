/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AdvancedQueryCachingTest extends AbstractCoreTest {

    /**
     * Test with two different parameter list sizes to make sure the query cache isn't hit.
     * If it would, the second query fails because of the different parameter count.
     *
     * This test is for issue #381
     */
    @Test
    // NOTE: This uses advanced SQL that isn't supported for other JPA providers yet
    @Category({ NoEclipselink.class })
    public void differentParameterListSizesShouldNotResultInQueryCacheHit() {
        toUpperDocumentNames(Arrays.asList(1L));
        toUpperDocumentNames(Arrays.asList(1L, 2L));
    }

    private int toUpperDocumentNames(Collection<Long> ids) {
        return cbf.update(em, Document.class)
                .setExpression("name", "UPPER(name)")
                .where("id").in(ids)
                .executeWithReturning("id", Long.class)
                .getUpdateCount();
    }
}
