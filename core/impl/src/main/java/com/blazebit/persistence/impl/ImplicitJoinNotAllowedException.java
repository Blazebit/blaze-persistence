/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.lang.StringUtils;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ImplicitJoinNotAllowedException extends RuntimeException {

    private final JoinNode baseNode;
    private final List<String> joinRelationAttributes;
    private final String treatType;

    public ImplicitJoinNotAllowedException(JoinNode baseNode, List<String> joinRelationAttributes, String treatType) {
        this.baseNode = baseNode;
        this.joinRelationAttributes = joinRelationAttributes;
        this.treatType = treatType;
    }

    public JoinNode getBaseNode() {
        return baseNode;
    }

    public String getJoinRelationName() {
        return StringUtils.join(".", joinRelationAttributes);
    }

    public List<String> getJoinRelationAttributes() {
        return joinRelationAttributes;
    }

    public String getTreatType() {
        return treatType;
    }
}
