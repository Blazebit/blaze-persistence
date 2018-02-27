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

package com.blazebit.persistence.integration.hibernate.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ResultSetInvocationHandler implements InvocationHandler {

    private static final Map<String, Method> METHODS = new HashMap<String, Method>();
    private static final Method GET_OBJECT_FOR_CLASS_METHOD;
    private static final Method GET_OBJECT_FOR_MAP_METHOD;
    
    static {
        try {
            METHODS.put("getString", ResultSet.class.getMethod("getString", int.class));
            METHODS.put("getBoolean", ResultSet.class.getMethod("getBoolean", int.class));
            METHODS.put("getByte", ResultSet.class.getMethod("getByte", int.class));
            METHODS.put("getShort", ResultSet.class.getMethod("getShort", int.class));
            METHODS.put("getInt", ResultSet.class.getMethod("getInt", int.class));
            METHODS.put("getLong", ResultSet.class.getMethod("getLong", int.class));
            METHODS.put("getFloat", ResultSet.class.getMethod("getFloat", int.class));
            METHODS.put("getDouble", ResultSet.class.getMethod("getDouble", int.class));
            METHODS.put("getBigDecimal", ResultSet.class.getMethod("getBigDecimal", int.class));
            METHODS.put("getBytes", ResultSet.class.getMethod("getBytes", int.class));
            METHODS.put("getDate", ResultSet.class.getMethod("getDate", int.class));
            METHODS.put("getTime", ResultSet.class.getMethod("getTime", int.class));
            METHODS.put("getTimestamp", ResultSet.class.getMethod("getTimestamp", int.class));
            METHODS.put("getAsciiStream", ResultSet.class.getMethod("getAsciiStream", int.class));
            METHODS.put("getUnicodeStream", ResultSet.class.getMethod("getUnicodeStream", int.class));
            METHODS.put("getBinaryStream", ResultSet.class.getMethod("getBinaryStream", int.class));
            METHODS.put("getObject", ResultSet.class.getMethod("getObject", int.class));
            METHODS.put("getCharacterStream", ResultSet.class.getMethod("getCharacterStream", int.class));
            METHODS.put("getRef", ResultSet.class.getMethod("getRef", int.class));
            METHODS.put("getBlob", ResultSet.class.getMethod("getBlob", int.class));
            METHODS.put("getClob", ResultSet.class.getMethod("getClob", int.class));
            METHODS.put("getArray", ResultSet.class.getMethod("getArray", int.class));
            METHODS.put("getURL", ResultSet.class.getMethod("getURL", int.class));
            METHODS.put("getRowId", ResultSet.class.getMethod("getRowId", int.class));
            METHODS.put("getNClob", ResultSet.class.getMethod("getNClob", int.class));
            METHODS.put("getSQLXML", ResultSet.class.getMethod("getSQLXML", int.class));
            METHODS.put("getNString", ResultSet.class.getMethod("getNString", int.class));
            METHODS.put("getNCharacterStream", ResultSet.class.getMethod("getNCharacterStream", int.class));

            METHODS.put("getBigDecimal2", ResultSet.class.getMethod("getBigDecimal", int.class, int.class));
            METHODS.put("getDate2", ResultSet.class.getMethod("getDate", int.class, Calendar.class));
            METHODS.put("getTime2", ResultSet.class.getMethod("getTime", int.class, Calendar.class));
            METHODS.put("getTimestamp2", ResultSet.class.getMethod("getTimestamp", int.class, Calendar.class));
            
            GET_OBJECT_FOR_CLASS_METHOD = ResultSet.class.getMethod("getObject", int.class, Class.class);
            GET_OBJECT_FOR_MAP_METHOD = ResultSet.class.getMethod("getObject", int.class, Map.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

    }
    
    private final ResultSet delegate;
    private final Map<String, Integer> aliasIndex;
    private final HibernateReturningResult<?> returningResult;
    private final boolean calculateRowCount;
    private int rowCount = 0;

    public ResultSetInvocationHandler(ResultSet delegate, Map<String, Integer> aliasIndex, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.aliasIndex = aliasIndex;
        this.returningResult = returningResult;
        this.calculateRowCount = returningResult != null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method indexMethod;
        int parameterCount = method.getParameterTypes().length;
        if (parameterCount == 1 && (indexMethod = METHODS.get(method.getName())) != null) {
            return indexMethod.invoke(delegate, aliasIndex.get(args[0]));
        } else if (parameterCount > 1) {
            if ((indexMethod = METHODS.get(method.getName() + parameterCount)) != null) {
                Object[] newArgs = new Object[args.length];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[0] = aliasIndex.get(args[0]);
                return indexMethod.invoke(delegate, newArgs);
            } else if ("getObject".equals(method.getName())) {
                Object[] newArgs = new Object[args.length];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[0] = aliasIndex.get(args[0]);
                
                // Special handling for getObject
                if (method.getParameterTypes()[1].equals(Class.class)) {
                    return GET_OBJECT_FOR_CLASS_METHOD.invoke(delegate, newArgs);
                } else {
                    return GET_OBJECT_FOR_MAP_METHOD.invoke(delegate, newArgs);
                }
            }
        } else if (calculateRowCount) {
            if ("next".equals(method.getName())) {
                Object result = method.invoke(delegate, args);
                if ((Boolean) result) {
                    rowCount++;
                }
                return result;
            } else if ("close".equals(method.getName())) {
                returningResult.setUpdateCount(rowCount);
            }
        }
        
        return method.invoke(delegate, args);
    }

}
