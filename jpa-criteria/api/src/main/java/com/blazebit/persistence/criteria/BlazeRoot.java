package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Root;

public interface BlazeRoot<X> extends Root<X>, BlazeFrom<X, X> {

    
}
