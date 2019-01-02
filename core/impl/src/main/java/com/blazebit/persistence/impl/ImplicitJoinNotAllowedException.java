/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
