/*
 * Copyright 2014 - 2024 Blazebit.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NClobBasicUserType extends AbstractLobBasicUserType<NClob> {

    public static final BasicUserType<?> INSTANCE = new NClobBasicUserType();

    @Override
    public NClob fromString(CharSequence s) {
        final String string = s.toString();
        return new NClob() {
            @Override
            public long length() throws SQLException {
                return string.length();
            }

            @Override
            public String getSubString(long pos, int length) throws SQLException {
                return string.substring((int) pos, (int) pos + length);
            }

            @Override
            public Reader getCharacterStream() throws SQLException {
                return new StringReader(string);
            }

            @Override
            public InputStream getAsciiStream() throws SQLException {
                return new StringBufferInputStream(string);
            }

            @Override
            public long position(String searchstr, long start) throws SQLException {
                return string.indexOf(searchstr, (int) start);
            }

            @Override
            public long position(Clob searchstr, long start) throws SQLException {
                return string.indexOf(searchstr.toString(), (int) start);
            }

            @Override
            public int setString(long pos, String str) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public int setString(long pos, String str, int offset, int len) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public OutputStream setAsciiStream(long pos) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public Writer setCharacterStream(long pos) throws SQLException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public void truncate(long len) throws SQLException {
            }

            @Override
            public void free() throws SQLException {
            }

            @Override
            public Reader getCharacterStream(long pos, long length) throws SQLException {
                return new StringReader(getSubString(pos, (int) length));
            }
        };
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
