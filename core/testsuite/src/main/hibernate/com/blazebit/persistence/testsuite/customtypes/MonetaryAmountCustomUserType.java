/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.testsuite.customtypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

import com.blazebit.persistence.testsuite.treat.entity.MonetaryAmount;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

/**
 * @author Emmanuel Bernard
 */
public class MonetaryAmountCustomUserType implements CompositeUserType {

    public String[] getPropertyNames() {
        return new String[]{"amount", "currency"};
    }

    public Type[] getPropertyTypes() {
        return new Type[]{ StandardBasicTypes.BIG_DECIMAL, StandardBasicTypes.CURRENCY };
    }

    public Object getPropertyValue(Object component, int property) throws HibernateException {
        MonetaryAmount ma = (MonetaryAmount) component;
        return property == 0 ? ma.getAmount() : ma.getCurrency();
    }

    public void setPropertyValue(Object component, int property, Object value)
            throws HibernateException {
        MonetaryAmount ma = (MonetaryAmount) component;
        if ( property == 0 ) {
            ma.setAmount( (BigDecimal) value );
        }
        else {
            ma.setCurrency( (Currency) value );
        }
    }

    public Class returnedClass() {
        return String.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if ( x == y ) return true;
        if ( x == null || y == null ) return false;
        MonetaryAmount mx = (MonetaryAmount) x;
        MonetaryAmount my = (MonetaryAmount) y;
        return mx.getAmount().equals( my.getAmount() ) &&
                mx.getCurrency().equals( my.getCurrency() );
    }

    public int hashCode(Object x) throws HibernateException {
        return ( (MonetaryAmount) x ).getAmount().hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        BigDecimal amt = StandardBasicTypes.BIG_DECIMAL.nullSafeGet( rs, names[0], session);
        Currency cur = StandardBasicTypes.CURRENCY.nullSafeGet( rs, names[1], session );
        if ( amt == null ) return null;
        return new MonetaryAmount( amt, cur );
    }

    public void nullSafeSet(
            PreparedStatement st, Object value, int index,
            SharedSessionContractImplementor session
    ) throws HibernateException, SQLException {
        MonetaryAmount ma = (MonetaryAmount) value;
        BigDecimal amt = ma == null ? null : ma.getAmount();
        Currency cur = ma == null ? null : ma.getCurrency();
        StandardBasicTypes.BIG_DECIMAL.nullSafeSet( st, amt, index, session );
        StandardBasicTypes.CURRENCY.nullSafeSet( st, cur, index + 1, session );
    }

    public Object deepCopy(Object value) throws HibernateException {
        MonetaryAmount ma = (MonetaryAmount) value;
        return new MonetaryAmount( ma.getAmount(), ma.getCurrency() );
    }

    public boolean isMutable() {
        return true;
    }

    public Serializable disassemble(Object value, SharedSessionContractImplementor session)
            throws HibernateException {
        return (Serializable) deepCopy( value );
    }

    public Object assemble(Serializable cached, SharedSessionContractImplementor session, Object owner)
            throws HibernateException {
        return deepCopy( cached );
    }

    public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner)
            throws HibernateException {
        return deepCopy( original ); //TODO: improve
    }

}