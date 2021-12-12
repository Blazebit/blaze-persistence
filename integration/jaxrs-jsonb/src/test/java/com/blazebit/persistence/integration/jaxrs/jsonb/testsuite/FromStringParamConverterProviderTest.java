/*
 * Copyright 2014 - 2021 Blazebit.
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
package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite;

import com.blazebit.persistence.integration.jaxrs.jsonb.EntityViewMessageBodyReader;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class FromStringParamConverterProviderTest {

    private static ParamConverterProvider FROM_STRING_PARAM_CONVERTER_PROVIDER;

    @BeforeClass
    public static void prepare() throws IllegalAccessException, InstantiationException {
        Class<? extends ParamConverterProvider> fromStringParamConverterProviderClass = null;
        for (Class<?> declaredClass : EntityViewMessageBodyReader.class.getDeclaredClasses()) {
            if ("FromStringParamConverterProvider".equals(declaredClass.getSimpleName())) {
                fromStringParamConverterProviderClass = (Class<ParamConverterProvider>) declaredClass;
                break;
            }
        }
        try {
            Constructor<? extends ParamConverterProvider> fromStringParamConverterProviderConstructor = fromStringParamConverterProviderClass.getDeclaredConstructor();
            fromStringParamConverterProviderConstructor.setAccessible(true);
            FROM_STRING_PARAM_CONVERTER_PROVIDER = fromStringParamConverterProviderConstructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void convertInt() {
       assertEquals(1, (int) convert(int.class, "1"));
    }

    @Test
    public void convertLong() {
       assertEquals(1, (long) convert(long.class, "1"));
    }

    @Test
    public void convertFloat() {
       assertEquals(1.01f, convert(float.class, "1.01"), 0.001);
    }

    @Test
    public void convertDouble() {
       assertEquals(1.01, convert(double.class, "1.01"), 0.001);
    }

    @Test
    public void convertBoolean() {
       assertEquals(true, convert(boolean.class, "true"));
    }

    @Test
    public void convertShort() {
       assertEquals(1, (short) convert(short.class, "1"));
    }

    @Test
    public void convertByte() {
       assertEquals(1, (byte) convert(byte.class, "1"));
    }

    @Test
    public void convertChar() {
       assertEquals('a', (char) convert(char.class, "a"));
    }

    private <T> T convert(Class<T> targetType, String value) {
        ParamConverter<T> converter = FROM_STRING_PARAM_CONVERTER_PROVIDER.getConverter(targetType, targetType, new Annotation[0]);
        return converter == null ? null : converter.fromString(value);
    }
}
