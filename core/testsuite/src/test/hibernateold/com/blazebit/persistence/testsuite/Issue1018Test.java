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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;
import org.hibernate.HibernateException;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.junit.Test;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Issue1018Test extends AbstractCoreTest  {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                EnumTestEntity.class
        };
    }

    public static enum MyEnum {
        VAL1(10),
        VAL2(20);

        private final int value;

        MyEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MyEnum byValue(int value) {
            switch (value) {
                case 10: return VAL1;
                case 20: return VAL2;
            }
            return null;
        }
    }

    public static class MyEnumUserType implements UserType {

        @Override
        public Class<MyEnum> returnedClass() {
            return MyEnum.class;
        }

        public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
            return nullSafeGet(rs, names, (SessionImplementor) null, owner);
        }

        public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
            int id = rs.getInt(names[0]);
            if (rs.wasNull()) {
                return null;
            }
            return MyEnum.byValue(id);
        }

        public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
            nullSafeSet(st, value, index, (SessionImplementor) null);
        }

        public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
            if (value == null) {
                st.setNull(index, Types.INTEGER);
            } else {
                st.setInt(index, ((MyEnum) value).getValue());
            }
        }

        @Override
        public Object assemble(Serializable cached, Object owner) throws HibernateException {
            return cached;
        }

        @Override
        public Object deepCopy(Object value) throws HibernateException {
            return value;
        }

        @Override
        public Serializable disassemble(Object value) throws HibernateException {
            return (Serializable) value;
        }

        @Override
        public boolean equals(Object x, Object y) throws HibernateException {
            return x == y;
        }

        @Override
        public int hashCode(Object x) throws HibernateException {
            return x == null ? 0 : x.hashCode();
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public Object replace(Object original, Object target, Object owner) throws HibernateException {
            return original;
        }

        @Override
        public int[] sqlTypes() {
            return new int[] { java.sql.Types.INTEGER };
        }

    }

    @Entity(name = "EnumTestEntity")
    @Access(AccessType.FIELD)
    @TypeDef(name = "MyEnumUserType", typeClass = MyEnumUserType.class)
    public static class EnumTestEntity extends LongSequenceEntity {
        @Type(type = "MyEnumUserType")
        MyEnum myEnum;
    }

    @Test
    public void testEnumLiteral() {
        cbf.create(em, EnumTestEntity.class)
                .where("myEnum").inLiterals(MyEnum.VAL1, MyEnum.VAL2)
                .whereExpression("myEnum = " + MyEnum.class.getName() + ".VAL1")
                .getResultList();
    }
}
