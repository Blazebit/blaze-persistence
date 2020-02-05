/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Base64;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlobBasicUserType extends AbstractLobBasicUserType<Blob> {

    public static final BasicUserType<?> INSTANCE = new BlobBasicUserType();

    @Override
    public Blob fromString(CharSequence sequence) {
        final byte[] bytes = Base64.getDecoder().decode(sequence.toString());
        return new Blob() {
            @Override
            public long length() throws SQLException {
                return bytes.length;
            }

            @Override
            public byte[] getBytes(long pos, int length) throws SQLException {
                return Arrays.copyOfRange(bytes, (int) pos, length);
            }

            @Override
            public InputStream getBinaryStream() throws SQLException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public long position(byte[] pattern, long start) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public long position(Blob pattern, long start) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public int setBytes(long pos, byte[] bytes) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public OutputStream setBinaryStream(long pos) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public void truncate(long len) throws SQLException {
            }

            @Override
            public void free() throws SQLException {
            }

            @Override
            public InputStream getBinaryStream(long pos, long length) throws SQLException {
                return new ByteArrayInputStream(bytes);
            }
        };
    }

    @Override
    public String toStringExpression(String expression) {
        return "BASE64(" + expression + ")";
    }
}
