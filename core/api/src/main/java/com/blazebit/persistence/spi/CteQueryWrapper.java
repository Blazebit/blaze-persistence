/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import javax.persistence.Query;
import java.util.List;

/**
 * Interface for CTE queries.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface CteQueryWrapper {

    /**
     * Returns the list of queries that are participating in this aggregate query.
     *
     * @return The participating queries
     */
    public List<Query> getParticipatingQueries();
}
