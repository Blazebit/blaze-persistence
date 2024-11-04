/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface SelectInfoVisitor {

    void visit(SelectInfo selectInfo);
}
