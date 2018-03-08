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

package com.blazebit.persistence.view.testsuite.update.rollback;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.rollback.model.UpdatableDocumentRollbackView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateRollbackTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentRollbackView> {

    public EntityViewUpdateRollbackTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentRollbackView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateRollbacked() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();
        
        // When 1
        docView.setName("newDoc");
        updateWithRollback(docView);

        // Then 1
        restartTransactionAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", doc1.getName());

        // When 2
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testModifyAndUpdateRollbacked() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();
        
        // When
        docView.setName("newDoc");
        updateWithRollback(docView);

        // Then 1
        restartTransactionAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", doc1.getName());

        // When 2
        docView.setName("newDoc1");
        // Remove milliseconds because MySQL doesn't use that precision by default
        Date date = new Date();
        date.setTime(1000 * (date.getTime() / 1000));
        docView.setLastModified(date);
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
        assertEquals("newDoc1", docView.getName());
        assertEquals(date.getTime(), docView.getLastModified().getTime());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals(doc1.getLastModified().getTime(), docView.getLastModified().getTime());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
