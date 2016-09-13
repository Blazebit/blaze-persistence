package com.blazebit.persistence.view;

/**
 * The fetch strategy for an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum FetchStrategy {

    JOIN,
    SUBQUERY,
    SUBSELECT;
}
