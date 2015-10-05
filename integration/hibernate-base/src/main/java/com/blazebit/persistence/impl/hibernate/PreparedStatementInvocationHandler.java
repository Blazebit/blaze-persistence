package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class PreparedStatementInvocationHandler implements InvocationHandler {

    private final PreparedStatement delegate;
    private final boolean generated;
    private final Map<String, Integer> aliasIndex;
    private final HibernateReturningResult<?> returningResult;

    public PreparedStatementInvocationHandler(PreparedStatement delegate, boolean generated, String[][] columns, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.generated = generated;
        this.aliasIndex = new HashMap<String, Integer>(columns.length);
        this.returningResult = returningResult;
        
        for (int i = 0; i < columns.length; i++) {
            aliasIndex.put(columns[i][1], i + 1);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("executeQuery".equals(method.getName()) && method.getParameterTypes().length == 0) {
            returningResult.setUpdateCount(delegate.executeUpdate());
            ResultSet rs = delegate.getGeneratedKeys();
            
            if (generated) {
                return Proxy.newProxyInstance(rs.getClass().getClassLoader(), new Class[]{ ResultSet.class }, new ResultSetInvocationHandler(rs, aliasIndex));
            }
            
            return rs;
        }
        
        return method.invoke(delegate, args);
    }

}
