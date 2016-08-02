package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CommonQueryBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ImplicitParameterBinding {

    public void bind(CommonQueryBuilder<?> builder);
}
