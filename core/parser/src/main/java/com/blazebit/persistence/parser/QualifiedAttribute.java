/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import jakarta.persistence.metamodel.PluralAttribute;

/**
 * Super type for attributes like KEY/VALUE/ENTRY/INDEX
 * 
 * @author Christian Beikov
 * @since 1.2.0
 *
 */
public interface QualifiedAttribute {

    public PluralAttribute<?, ?, ?> getPluralAttribute();

    public String getQualificationExpression();

}
