package com.blazebit.persistence.criteria;

import javax.persistence.criteria.CommonAbstractCriteria;

public interface BlazeCommonAbstractCriteria extends CommonAbstractCriteria {

    /* Covariant overrides */

    @Override
    <U> BlazeSubquery<U> subquery(Class<U> type);
}
