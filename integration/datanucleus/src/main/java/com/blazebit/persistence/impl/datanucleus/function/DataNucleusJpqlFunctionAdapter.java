package com.blazebit.persistence.impl.datanucleus.function;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.datanucleus.store.rdbms.mapping.java.JavaTypeMapping;
import org.datanucleus.store.rdbms.mapping.java.TemporalMapping;
import org.datanucleus.store.rdbms.sql.expression.AggregateNumericExpression;
import org.datanucleus.store.rdbms.sql.expression.AggregateTemporalExpression;
import org.datanucleus.store.rdbms.sql.expression.BooleanExpression;
import org.datanucleus.store.rdbms.sql.expression.ByteExpression;
import org.datanucleus.store.rdbms.sql.expression.CharacterExpression;
import org.datanucleus.store.rdbms.sql.expression.NumericExpression;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.expression.StringExpression;
import org.datanucleus.store.rdbms.sql.expression.TemporalExpression;
import org.datanucleus.store.rdbms.sql.method.AbstractSQLMethod;

import com.blazebit.persistence.spi.JpqlFunction;

public class DataNucleusJpqlFunctionAdapter extends AbstractSQLMethod {
	
	private static final Logger LOG = Logger.getLogger(DataNucleusJpqlFunctionAdapter.class.getName());
	private final JpqlFunction function;
	private final boolean aggregate;

	public DataNucleusJpqlFunctionAdapter(JpqlFunction function, boolean aggregate) {
		this.function = function;
		this.aggregate = aggregate;
	}

	@Override
	public SQLExpression getExpression(SQLExpression expr, List<SQLExpression> args) {
		int argsSize = args.size();
        List<String> newArgs = new ArrayList<String>(argsSize);
        Class<?> firstArgumentType = null;
        JavaTypeMapping firstArgumentTypeMapping = null;
        if (argsSize > 0) {
        	firstArgumentTypeMapping = args.get(0).getJavaTypeMapping();
        	firstArgumentType = firstArgumentTypeMapping.getJavaType();
            newArgs.add(args.get(0).toSQLText().toSQL());
	        for (int i = 1; i < argsSize; i++) {
	            newArgs.add(args.get(i).toSQLText().toSQL());
	        }
        }
        
        final DataNucleusFunctionRenderContext context = new DataNucleusFunctionRenderContext(newArgs);
        function.render(context);
        
        Class<?> returnType = function.getReturnType(firstArgumentType);
        JavaTypeMapping returnTypeMapping;
        
        if (returnType == firstArgumentType) {
        	returnTypeMapping = firstArgumentTypeMapping;
        } else {
        	returnTypeMapping = getMappingForClass(returnType);
        }
        
        if (returnTypeMapping == null) {
        	throw new IllegalArgumentException("Invalid return type null returned from function: " + function);
        }
        
        if (aggregate) {
        	if (returnTypeMapping instanceof TemporalMapping) {
        		return new AggregateTemporalExpression(stmt, returnTypeMapping, "", null) {
        			{
                        st.clearStatement();
                        st.append(context.renderToString());
        			}
        		};
        	} else if (Number.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
        		return new AggregateNumericExpression(stmt, returnTypeMapping, context.renderToString());
        	} else {
        		LOG.warning("Aggregate type [" + returnType + "] could not be represented as aggregate. Please report this so we can support the type! Falling back to normal expression.");
        	}
        } else {
	        if (returnTypeMapping instanceof TemporalMapping) {
	    		return new TemporalExpression(stmt, returnTypeMapping, "", null) {
	    			{
	                    st.clearStatement();
	                    st.append(context.renderToString());
	    			}
	    		};
	    	} else if (Byte.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
	    		return new ByteExpression(stmt, null, returnTypeMapping) {
	    			{
	                    st.clearStatement();
	                    st.append(context.renderToString());
	    			}
	    		};
	    	} else if (Number.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
	    		return new NumericExpression(stmt, returnTypeMapping, context.renderToString());
	    	} else if (String.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
	    		return new StringExpression(stmt, null, returnTypeMapping) {
	    			{
	                    st.clearStatement();
	                    st.append(context.renderToString());
	    			}
	    		};
	    	} else if (Character.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
	    		return new CharacterExpression(stmt, null, returnTypeMapping) {
	    			{
	                    st.clearStatement();
	                    st.append(context.renderToString());
	    			}
	    		};
	    	} else if (Boolean.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
	    		return new BooleanExpression(stmt, returnTypeMapping, context.renderToString());
	    	} else {
	    		LOG.warning("Aggregate type [" + returnType + "] could not be represented as aggregate. Please report this so we can support the type! Falling back to normal expression.");
	    	}
        }
        
        return new SQLExpression(stmt, null, returnTypeMapping) {
        	{
                st.clearStatement();
                st.append(context.renderToString());
        	}
		};
	}

}
