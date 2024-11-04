/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import javax.persistence.metamodel.Type;

/**
 * TODO: documentation
 * 
 * @author Christian Beikov
 * @since 1.1.0
 *
 */
public interface PathReference {

    // Although this node will always be a JoinNode we will use casting at use site to be able to reuse the parser
    public BaseNode getBaseNode();
    
    public String getField();

    /**
     * Returns the type of the path reference.
     *
     * @return The type of the path
     * @since 1.2.0
     */
    public Type<?> getType();
}
