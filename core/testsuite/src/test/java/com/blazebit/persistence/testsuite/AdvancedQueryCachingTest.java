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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collection;

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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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
