package com.blazebit.persistence.impl.datanucleus.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

public class CountStarFunction implements JpqlFunction {

	@Override
	public boolean hasArguments() {
		return false;
	}

	@Override
	public boolean hasParenthesesIfNoArguments() {
		return true;
	}

	@Override
	public Class<?> getReturnType(Class<?> firstArgumentType) {
		return long.class;
	}

	@Override
	public void render(FunctionRenderContext context) {
		context.addChunk("COUNT(*)");
	}

}
