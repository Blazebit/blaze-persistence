package com.blazebit.persistence.impl.function.set;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class SetFunction implements JpqlFunction {

    protected final SetOperationType type;
    protected final DbmsDialect dbmsDialect;

    public SetFunction(SetOperationType type, DbmsDialect dbmsDialect) {
        // OPERATION(SUBQUERY, ...)
        this.type = type;
        this.dbmsDialect = dbmsDialect;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        if (functionRenderContext.getArgumentsSize() == 0) {
            throw new RuntimeException("The " + type + " function needs at least one argument <sub_query>! args=" + functionRenderContext);
        }
        
        int size = 0;
        List<String> operands = new ArrayList<String>(functionRenderContext.getArgumentsSize());
        for (int i = 0; i < functionRenderContext.getArgumentsSize(); i++) {
            String operand = functionRenderContext.getArgument(i);
            size += operand.length();
            operands.add(operand);
        }
        
        StringBuilder sqlSb = new StringBuilder(size + functionRenderContext.getArgumentsSize() * 12);
        dbmsDialect.appendSet(sqlSb, type, true, operands);
        functionRenderContext.addChunk(sqlSb.toString());
    }

}
