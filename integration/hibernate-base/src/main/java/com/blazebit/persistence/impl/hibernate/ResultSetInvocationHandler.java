package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ResultSetInvocationHandler implements InvocationHandler {

    private static final Map<String, Method> methods = new HashMap<String, Method>();
    private static final Method getObjectForClassMethod;
    private static final Method getObjectForMapMethod;
    
    static {
        try {
            methods.put("getString", ResultSet.class.getMethod("getString", int.class));
            methods.put("getBoolean", ResultSet.class.getMethod("getBoolean", int.class));
            methods.put("getByte", ResultSet.class.getMethod("getByte", int.class));
            methods.put("getShort", ResultSet.class.getMethod("getShort", int.class));
            methods.put("getInt", ResultSet.class.getMethod("getInt", int.class));
            methods.put("getLong", ResultSet.class.getMethod("getLong", int.class));
            methods.put("getFloat", ResultSet.class.getMethod("getFloat", int.class));
            methods.put("getDouble", ResultSet.class.getMethod("getDouble", int.class));
            methods.put("getBigDecimal", ResultSet.class.getMethod("getBigDecimal", int.class));
            methods.put("getBytes", ResultSet.class.getMethod("getBytes", int.class));
            methods.put("getDate", ResultSet.class.getMethod("getDate", int.class));
            methods.put("getTime", ResultSet.class.getMethod("getTime", int.class));
            methods.put("getTimestamp", ResultSet.class.getMethod("getTimestamp", int.class));
            methods.put("getAsciiStream", ResultSet.class.getMethod("getAsciiStream", int.class));
            methods.put("getUnicodeStream", ResultSet.class.getMethod("getUnicodeStream", int.class));
            methods.put("getBinaryStream", ResultSet.class.getMethod("getBinaryStream", int.class));
            methods.put("getObject", ResultSet.class.getMethod("getObject", int.class));
            methods.put("getCharacterStream", ResultSet.class.getMethod("getCharacterStream", int.class));
            methods.put("getRef", ResultSet.class.getMethod("getRef", int.class));
            methods.put("getBlob", ResultSet.class.getMethod("getBlob", int.class));
            methods.put("getClob", ResultSet.class.getMethod("getClob", int.class));
            methods.put("getArray", ResultSet.class.getMethod("getArray", int.class));
            methods.put("getURL", ResultSet.class.getMethod("getURL", int.class));
            methods.put("getRowId", ResultSet.class.getMethod("getRowId", int.class));
            methods.put("getNClob", ResultSet.class.getMethod("getNClob", int.class));
            methods.put("getSQLXML", ResultSet.class.getMethod("getSQLXML", int.class));
            methods.put("getNString", ResultSet.class.getMethod("getNString", int.class));
            methods.put("getNCharacterStream", ResultSet.class.getMethod("getNCharacterStream", int.class));

            methods.put("getBigDecimal2", ResultSet.class.getMethod("getBigDecimal", int.class, int.class));
            methods.put("getDate2", ResultSet.class.getMethod("getDate", int.class, Calendar.class));
            methods.put("getTime2", ResultSet.class.getMethod("getTime", int.class, Calendar.class));
            methods.put("getTimestamp2", ResultSet.class.getMethod("getTimestamp", int.class, Calendar.class));
            
            getObjectForClassMethod = ResultSet.class.getMethod("getObject", int.class, Class.class);
            getObjectForMapMethod = ResultSet.class.getMethod("getObject", int.class, Map.class);
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
        if (parameterCount == 1 && (indexMethod = methods.get(method.getName())) != null) {
            return indexMethod.invoke(delegate, aliasIndex.get(args[0]));
        } else if (parameterCount > 1) {
            if ((indexMethod = methods.get(method.getName() + parameterCount)) != null) {
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
                    return getObjectForClassMethod.invoke(delegate, newArgs);
                } else {
                    return getObjectForMapMethod.invoke(delegate, newArgs);
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
