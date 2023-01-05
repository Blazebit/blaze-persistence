/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.natural;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.natural.model.BookIdView;
import com.blazebit.persistence.view.testsuite.update.natural.model.BookIsbnView;
import com.blazebit.persistence.view.testsuite.update.natural.model.UpdatableNaturalJoinTableEntityWithMapsView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleImmutableSubviewMapsTest extends AbstractBookEntityViewTest<UpdatableNaturalJoinTableEntityWithMapsView> {

    public EntityViewUpdateSimpleImmutableSubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableNaturalJoinTableEntityWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(BookIdView.class);
        cfg.addEntityView(BookIsbnView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "manyToManyBook" };
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableNaturalJoinTableEntityWithMapsView docView = naturalJoinTableEntity(UpdatableNaturalJoinTableEntityWithMapsView.class, "123");
        clearQueries();
        
        // When
        docView.setManyToManyBook(new HashMap<>(docView.getManyToManyBook()));
        update(docView);

        // Then
        // Assert that the document and the people are loaded in full mode.
        // During dirty detection we should be able to figure out that nothing changed
        // So partial modes wouldn't load anything and both won't cause any updates
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                assertReplaceAnd(builder);
                if (version) {
                    versionUpdate(builder);
                }
            } else {
                fullFetch(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(e1.getManyToManyBook(), docView.getManyToManyBook());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableNaturalJoinTableEntityWithMapsView docView = naturalJoinTableEntity(UpdatableNaturalJoinTableEntityWithMapsView.class, "123");
        BookIsbnView newPerson = book(BookIsbnView.class, "789");
        clearQueries();
        
        // When
        docView.getManyToManyBook().put("b3", newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // We need to load the book we set by natural key
        // At some point we can optimize this though for Hibernate
        builder.select(BookEntity.class);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                assertReplaceAnd(builder);
            }
        } else {
            fullFetch(builder);
        }

        if (version) {
            builder.update(NaturalIdJoinTableEntity.class);
        }

        builder.insert(NaturalIdJoinTableEntity.class, "manyToManyBook")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(e1.getManyToManyBook(), docView.getManyToManyBook());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableNaturalJoinTableEntityWithMapsView docView = naturalJoinTableEntity(UpdatableNaturalJoinTableEntityWithMapsView.class, "123");
        BookIsbnView newPerson = book(BookIsbnView.class, "789");
        clearQueries();

        // When
        docView.setManyToManyBook(new HashMap<>(docView.getManyToManyBook()));
        docView.getManyToManyBook().put("b3", newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // We need to load the book we set by natural key
        // At some point we can optimize this though for Hibernate
        builder.select(BookEntity.class);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                assertReplaceAnd(builder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                if (preferLoadingAndDiffingOverRecreate()) {
                    fullFetch(builder);
                } else {
                    assertReplaceAnd(builder);
                }
            }
        }

        if (version) {
            builder.update(NaturalIdJoinTableEntity.class);
        }

        builder.insert(NaturalIdJoinTableEntity.class, "manyToManyBook")
                .validate();
        assertNoUpdateAndReload(docView);
        assertSubviewEquals(e1.getManyToManyBook(), docView.getManyToManyBook());
    }

    public static void assertSubviewEquals(Map<String, BookEntity> persons, Map<String, BookIsbnView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<String, BookEntity> entry : persons.entrySet()) {
            BookEntity p = entry.getValue();
            boolean found = false;
            BookIsbnView pSub = personSubviews.get(entry.getKey());
            if (pSub != null) {
                if (p.getIsbn().equals(pSub.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getIsbn());
            }
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        builder.delete(NaturalIdJoinTableEntity.class, "manyToManyBook")
                .insert(NaturalIdJoinTableEntity.class, "manyToManyBook")
                .insert(NaturalIdJoinTableEntity.class, "manyToManyBook");
        // This can be removed when we support natural id references
        builder.select(BookEntity.class);
        builder.select(BookEntity.class);
        if (e1.getManyToManyBook().size() > 2) {
            builder.select(BookEntity.class);
            builder.insert(NaturalIdJoinTableEntity.class, "manyToManyBook");
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        if (version) {
            versionUpdate(builder);
        }
        return assertReplaceAnd(builder);
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        builder.assertSelect()
                .fetching(NaturalIdJoinTableEntity.class)
                .fetching(NaturalIdJoinTableEntity.class, "manyToManyBook")
                .fetching(BookEntity.class)
                .and();
        if (!supportsSingleStatementNaturalIdCollectionFetching()) {
            builder.select(BookEntity.class);
            builder.select(BookEntity.class);
            if (e1.getManyToManyBook().size() > 2) {
                builder.select(BookEntity.class);
            }
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(NaturalIdJoinTableEntity.class);
    }
}
