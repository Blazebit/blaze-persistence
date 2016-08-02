package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Fetch;

public interface BlazeFetch<Z, X> extends Fetch<Z, X>, BlazeFetchParent<Z, X> {

    /* Covariant overrides */
    
    BlazeFetchParent<?, Z> getParent();
    
}
