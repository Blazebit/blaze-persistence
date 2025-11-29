/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
class CTEInfo {
    final String name;
    final JoinManager owner;
    final boolean inline;
    final EntityType<?> cteType;
    final List<String> attributes;
    final List<String> columnNames;
    final boolean recursive;
    final boolean unionAll;
    final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> nonRecursiveCriteriaBuilder;
    final SelectCTECriteriaBuilderImpl<?> recursiveCriteriaBuilder;
    
    CTEInfo(String name, JoinManager owner, boolean inline, EntityType<?> cteType, List<String> attributes, List<String> columnNames, boolean recursive, boolean unionAll, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> nonRecursiveCriteriaBuilder, SelectCTECriteriaBuilderImpl<?> recursiveCriteriaBuilder) {
        this.name = name;
        this.owner = owner;
        this.inline = inline;
        this.cteType = cteType;
        this.attributes = attributes;
        this.columnNames = columnNames;
        this.recursive = recursive;
        this.unionAll = unionAll;
        this.nonRecursiveCriteriaBuilder = nonRecursiveCriteriaBuilder;
        this.recursiveCriteriaBuilder = recursiveCriteriaBuilder;
    }

    CTEInfo copy(CTEManager cteManager, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        CTEInfo cteInfo = new CTEInfo(
                name,
                joinManagerMapping.get(owner),
                inline,
                cteType,
                attributes,
                columnNames,
                recursive,
                unionAll,
                nonRecursiveCriteriaBuilder.copy(cteManager.getQueryContext(), joinManagerMapping, copyContext),
                recursive ? recursiveCriteriaBuilder.copy(cteManager.getQueryContext(), joinManagerMapping, copyContext) : null
        );

        return cteInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CTEInfo other = (CTEInfo) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}