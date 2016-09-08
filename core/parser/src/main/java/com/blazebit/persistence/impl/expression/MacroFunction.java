package com.blazebit.persistence.impl.expression;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MacroFunction {

    public Expression apply(List<Expression> expressions);

}
