package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Order;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeOrder extends Order {

    BlazeOrder reverseNulls();

    boolean isNullsFirst();

    /* covariant overrides */

    BlazeOrder reverse();

}
