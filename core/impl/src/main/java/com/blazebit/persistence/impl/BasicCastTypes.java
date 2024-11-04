/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public interface BasicCastTypes {

    Set<Class<?>> TYPES = new HashSet<>(Arrays.<Class<?>>asList(
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,

            Character.class,
            String.class,

            BigInteger.class,
            BigDecimal.class,

            Time.class,
            java.sql.Date.class,
            Timestamp.class,
            Calendar.class
    ));
}
