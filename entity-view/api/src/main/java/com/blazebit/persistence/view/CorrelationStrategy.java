package com.blazebit.persistence.view;

/**
 * The correlation strategy for a correlation provider.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum CorrelationStrategy {

    JOIN,
    SUBQUERY,
    BATCH;
}
