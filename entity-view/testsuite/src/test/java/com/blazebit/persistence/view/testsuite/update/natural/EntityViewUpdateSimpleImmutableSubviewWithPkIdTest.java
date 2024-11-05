/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.natural;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.natural.model.BookIsbnWithPkView;
import com.blazebit.persistence.view.testsuite.update.natural.model.BookPkWithIsbnView;
import com.blazebit.persistence.view.testsuite.update.natural.model.UpdatableBookReferencePkView;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public class EntityViewUpdateSimpleImmutableSubviewWithPkIdTest extends AbstractBookEntityViewTest<UpdatableBookReferencePkView> {

    public EntityViewUpdateSimpleImmutableSubviewWithPkIdTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableBookReferencePkView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(BookPkWithIsbnView.class);
        cfg.addEntityView(BookIsbnWithPkView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final UpdatableBookReferencePkView docView = evm.find(em, UpdatableBookReferencePkView.class, e2.getId());
        BookIsbnWithPkView book123 = book(BookIsbnWithPkView.class, "123");
        BookPkWithIsbnView book456 = book(BookPkWithIsbnView.class, "456");
        clearQueries();
        
        // When
        docView.setBookNormal(book123);
        docView.setBook(book456);
        update(docView);

        // Then
        // Assert that only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // We need to load the book we set by natural key
        // At some point we can optimize this though for Hibernate
        builder.select(BookEntity.class);

        if (!isQueryStrategy()) {
            builder.select(BookISBNReferenceEntity.class).select(BookEntity.class);;
        }

        builder.update(BookISBNReferenceEntity.class)
                .validate();

        assertNoUpdateAndReload(docView);
        clearPersistenceContextAndReload();
        assertEquals("456", e2.getBook().getIsbn());
        assertEquals("123", e2.getBookNormal().getIsbn());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        builder.assertSelect()
                .fetching(BookISBNReferenceEntity.class)
                .and();
        builder.select(BookEntity.class);
        if (!isQueryStrategy()) {
            builder.select(BookEntity.class);
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.select(BookEntity.class);
        return builder.update(BookISBNReferenceEntity.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(BookISBNReferenceEntity.class);
    }
}
