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

package com.blazebit.persistence.view.testsuite.update.converter;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.BlobEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.converter.model.UpdatableBlobEntityView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
// NOTE: Not sure why, but Hibernate 4.2 reports the LOB as being closed
@Category({ NoHibernate42.class, NoDatanucleus.class, NoEclipselink.class})
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
    protected void restartTransactionAndReload() {
        restartTransaction();
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
        restartTransactionAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", entity.getName());

        // When 2
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
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
        restartTransactionAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", entity.getName());

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
        assertEquals(entity.getName(), docView.getName());
        assertEquals(entity.getLastModified().getTime(), docView.getLastModified().getTime());
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
        restartTransactionAndReload();
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
            restartTransactionAndReload();
        }
        final UpdatableBlobEntityView docView = getBlobView();
        clearQueries();

        // When
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();
        if (isFullMode()) {
            if (!isQueryStrategy()) {
                fullFetch(builder);
            }
            builder.update(BlobEntity.class);
        }
        builder.validate();
        restartTransactionAndReload();
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
        AssertStatementBuilder builder = assertQuerySequence();
        if (!isQueryStrategy()) {
            fullFetch(builder);
        }
        builder.update(BlobEntity.class);
        builder.validate();

        assertEquals(1, docView.getBlob().length());
        restartTransactionAndReload();
        assertEquals(1, entity.getBlob().length());
    }

    @Test
    // Oracle keeps LOBs open/tied to a result set so we can't write to it by means of setBytes.
    // For Oracle it would be more appropriate to treat writes like "replacements" i.e. remember the written bytes and replace the underlying object on flush
    // NOTE: No Datanucleus support yet
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOracle.class })
    public void testUpdateBlob() throws Exception {
        // Given
        {
            final UpdatableBlobEntityView docView = getBlobView();
            docView.setBlob(new BlobImpl(new byte[1]));
            update(docView);
            restartTransactionAndReload();
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
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        AssertStatementBuilder builder = assertQuerySequence();
        if (!isQueryStrategy()) {
            fullFetch(builder);
        }
        builder.update(BlobEntity.class);
        builder.validate();

        assertEquals(2, docView.getBlob().length());
        restartTransactionAndReload();
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
