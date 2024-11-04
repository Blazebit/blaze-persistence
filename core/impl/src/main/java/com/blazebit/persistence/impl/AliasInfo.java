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
public interface AliasInfo {

    public String getAlias();

    public AliasManager getAliasOwner();
}
