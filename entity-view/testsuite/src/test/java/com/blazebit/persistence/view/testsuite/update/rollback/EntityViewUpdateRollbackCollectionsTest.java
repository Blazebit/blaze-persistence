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
import com.blazebit.persistence.view.testsuite.update.rollback.model.UpdatableDocumentRollbackWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateRollbackCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentRollbackWithCollectionsView> {

    public EntityViewUpdateRollbackCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentRollbackWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentRollbackWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When 1
        docView.getStrings().add("newString");
        updateWithRollback(docView);

        // Then 1
        restartTransactionAndReload();
        assertEquals(1, doc1.getStrings().size());
        clearQueries();

        // When 2
        update(docView);

        // Then 2
        // Assert that the document and the strings are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "strings")
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getStrings(), docView.getStrings());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentRollbackWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When
        docView.setStrings(new ArrayList<>(docView.getStrings()));
        docView.getStrings().add("newString");
        updateWithRollback(docView);

        // Then 1
        restartTransactionAndReload();
        assertEquals(1, doc1.getStrings().size());
        clearQueries();

        // When 2
        update(docView);

        // Then 2
        // In partial mode, only the document is loaded. In full mode, the strings are also loaded
        // Since we load the strings in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                assertReplaceAnd(builder);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                    .forRelation(Document.class, "strings")
                .validate();
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getStrings(), docView.getStrings());
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "strings")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "strings")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "strings")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        if (version) {
            return versionUpdate(fullFetch(builder));
        } else {
            return fullFetch(builder);
        }
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
