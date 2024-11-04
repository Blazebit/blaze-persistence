/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface JoinNodeVisitor {

    public void visit(JoinNode node);
}
