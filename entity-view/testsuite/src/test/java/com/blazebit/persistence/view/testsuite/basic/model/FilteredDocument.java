/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.GreaterOrEqualFilter;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
@ViewFilters({
    @ViewFilter(name = "testFilter", value = FilteredDocument.TestFilter.class)
})
public interface FilteredDocument {
    
    @IdMapping
    public Long getId();

    @AttributeFilter(ContainsFilter.class)
    public String getName();

    @AttributeFilter(GreaterOrEqualFilter.class)
    @MappingSubquery(CountSubqueryProvider.class)
    public Long getContactCount();

    @Mapping("contacts[:index].name")
    public String getContactName();
    
    public static class TestFilter extends ViewFilterProvider {

        @Override
        public <T extends WhereBuilder<T>> T apply(T whereBuilder) {
            return whereBuilder.where("age").gt(2L);
        }
        
    }
}
