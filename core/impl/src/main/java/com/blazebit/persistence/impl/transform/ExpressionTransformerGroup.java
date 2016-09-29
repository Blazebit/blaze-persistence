package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AbstractManager;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 26.09.2016.
 */
public interface ExpressionTransformerGroup {

    void applyExpressionTransformer(AbstractManager manager);

    void afterGlobalTransformation();
}
