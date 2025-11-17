/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.converter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.BlobEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.converter.model.UpdatableBlobEntityView;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
@Category({ NoEclipselink.class})
public class EntityViewUpdateBlobTest extends AbstractEntityViewUpdateTest<UpdatableBlobEntityView> {

    private BlobEntity entity;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                BlobEntity.class
        };
    }

    public EntityViewUpdateBlobTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableBlobEntityView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void prepareData(EntityManager em) {
        BlobEntity e = new BlobEntity();
        e.setName("doc1");
        e.setVersion(0L);
        e.setLastModified(new Date(EPOCH_2K));
        em.persist(e);
    }

    @Override
    protected void reload() {
        entity = cbf.create(em, BlobEntity.class).getSingleResult();
    }

    private UpdatableBlobEntityView getBlobView() {
        return evm.applySetting(EntityViewSetting.create(UpdatableBlobEntityView.class), cbf.create(em, BlobEntity.class)).getSingleResult();
    }

    @Test
    public void testUpdateRollbacked() {
        // Given
        final UpdatableBlobEntityView docView = getBlobView();

        // When 1
        docView.setName("newDoc");
        updateWithRollback(docView);

        // Then 1
        clearPersistenceContextAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", entity.getName());

        // When 2
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView, true);
        assertEquals("newDoc", docView.getName());
        assertEquals(entity.getName(), docView.getName());
    }

    @Test
    public void testModifyAndUpdateRollbacked() {
        // Given
        final UpdatableBlobEntityView docView = getBlobView();

        // When
        docView.setName("newDoc");
        updateWithRollback(docView);

        // Then 1
        clearPersistenceContextAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", entity.getName());

        // When 2
        docView.setName("newDoc1");
        // Remove milliseconds because MySQL doesn't use that precision by default
        Instant date = Instant.ofEpochMilli(1000 * (System.currentTimeMillis() / 1000));
        docView.setLastModified(date);
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView, true);
        assertEquals("newDoc1", docView.getName());
        assertEquals(date.toEpochMilli(), docView.getLastModified().toEpochMilli());
        assertEquals(entity.getName(), docView.getName());
        assertEquals(entity.getLastModified().getTime(), docView.getLastModified().toEpochMilli());
    }

    @Test
    public void testUpdateNothing() throws Exception {
        // Given
        final UpdatableBlobEntityView docView = getBlobView();
        clearQueries();

        // When
        update(docView);

        // Then
        validateNoChange(docView);
        clearPersistenceContextAndReload();
        assertEquals("doc1", docView.getName());
        assertEquals(entity.getName(), docView.getName());
    }

    @Test
    public void testUpdateNothingWhenExistingBlob() throws Exception {
        // Given
        {
            final UpdatableBlobEntityView docView = getBlobView();
            docView.setBlob(new BlobImpl(new byte[1]));
            update(docView);
            clearPersistenceContextAndReload();
        }
        final UpdatableBlobEntityView docView = getBlobView();
        clearQueries();

        // When
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();
        if (isFullMode()) {
            if (!isQueryStrategy()) {
                fullFetch(builder);
            }
            builder.update(BlobEntity.class);
        }
        builder.validate();
        clearPersistenceContextAndReload();
        assertEquals("doc1", docView.getName());
        assertEquals(entity.getName(), docView.getName());
    }

    @Test
    public void testSetBlob() throws Exception {
        // Given
        final UpdatableBlobEntityView docView = getBlobView();
        clearQueries();

        // When
        docView.setBlob(new BlobImpl(new byte[1]));
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();
        if (!isQueryStrategy()) {
            fullFetch(builder);
        }
        builder.update(BlobEntity.class);
        builder.validate();

        assertEquals(1, docView.getBlob().length());
        clearPersistenceContextAndReload();
        assertEquals(1, entity.getBlob().length());
    }

    @Test
    // Oracle keeps LOBs open/tied to a result set so we can't write to it by means of setBytes.
    // For Oracle it would be more appropriate to treat writes like "replacements" i.e. remember the written bytes and replace the underlying object on flush
    @Category({ NoEclipselink.class, NoOracle.class, NoH2.class})
    public void testUpdateBlob() throws Exception {
        // Given
        {
            final UpdatableBlobEntityView docView = getBlobView();
            docView.setBlob(new BlobImpl(new byte[1]));
            update(docView);
            clearPersistenceContextAndReload();
        }
        final UpdatableBlobEntityView docView = getBlobView();
        clearQueries();

        // When
        docView.getBlob().setBytes(1, new byte[2]);
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                em.clear();
                clearQueries();
                evm.save(em, docView);
                em.flush();
            }
        });

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();
        if (!isQueryStrategy()) {
            fullFetch(builder);
        }
        builder.update(BlobEntity.class);
        builder.validate();

        assertEquals(2, docView.getBlob().length());
        clearPersistenceContextAndReload();
        assertEquals(2, entity.getBlob().length());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(BlobEntity.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(BlobEntity.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(BlobEntity.class);
    }

    private static class BlobImpl implements Blob {
        private final byte[] bytes;

        public BlobImpl(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public long length() throws SQLException {
            return bytes.length;
        }

        @Override
        public byte[] getBytes(long pos, int length) throws SQLException {
            return bytes;
        }

        @Override
        public InputStream getBinaryStream() throws SQLException {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public long position(byte[] pattern, long start) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public long position(Blob pattern, long start) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public int setBytes(long pos, byte[] bytes) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public OutputStream setBinaryStream(long pos) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public void truncate(long len) throws SQLException {
            throw new SQLException("Unsupported");
        }

        @Override
        public void free() throws SQLException {
        }

        @Override
        public InputStream getBinaryStream(long pos, long length) throws SQLException {
            return new ByteArrayInputStream(bytes, (int) pos - 1, (int) length);
        }
    }
}
