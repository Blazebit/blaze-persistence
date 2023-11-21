/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewTypeObjectBuilder<T> implements ObjectBuilder<T> {

    final boolean hasId;
    final boolean nullIfEmpty;
    private final ObjectInstantiator<T> objectInstantiator;
    private final TupleElementMapper[] mappers;
    private final ParameterHolder<?> parameterHolder;
    private final Map<String, Object> optionalParameters;
    private final ViewJpqlMacro viewJpqlMacro;
    private final EmbeddingViewJpqlMacro embeddingViewJpqlMacro;
    private final NavigableSet<String> fetches;
    private final SecondaryMapper[] secondaryMappers;

    public ViewTypeObjectBuilder(ViewTypeObjectBuilderTemplate<T> template, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, NavigableSet<String> fetches, boolean nullIfEmpty) {
        this.hasId = template.hasId();
        this.objectInstantiator = template.getObjectInstantiator();
        this.mappers = template.getMappers();
        this.parameterHolder = parameterHolder;
        this.optionalParameters = optionalParameters == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(optionalParameters);
        this.viewJpqlMacro = viewJpqlMacro;
        this.embeddingViewJpqlMacro = embeddingViewJpqlMacro;
        this.fetches = fetches;
        this.nullIfEmpty = nullIfEmpty;
        this.secondaryMappers = template.getSecondaryMappers();
    }

    @Override
    public T build(Object[] tuple) {
        if (hasId) {
            if (tuple[0] == null) {
                return null;
            }
        } else if (nullIfEmpty) {
            for (int i = 0; i < tuple.length; i++) {
                if (tuple[i] != null) {
                    return objectInstantiator.newInstance(tuple);
                }
            }

            return null;
        }

        return objectInstantiator.newInstance(tuple);
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        if (fetches == null || fetches.isEmpty()) {
            if (secondaryMappers.length != 0) {
                FullQueryBuilder<?, ?> fullQueryBuilder = (FullQueryBuilder<?, ?>) queryBuilder;
                for (SecondaryMapper viewRoot : secondaryMappers) {
                    viewRoot.apply(fullQueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro);
                }
            }
            for (int i = 0; i < mappers.length; i++) {
                mappers[i].applyMapping(queryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro, false);
            }
        } else {
            if (secondaryMappers.length != 0) {
                FullQueryBuilder<?, ?> fullQueryBuilder = (FullQueryBuilder<?, ?>) queryBuilder;
                for (SecondaryMapper viewRoot : secondaryMappers) {
                    if (hasSubFetches(fetches, viewRoot.getAttributePath())) {
                        viewRoot.apply(fullQueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro);
                    }
                }
            }
            for (int i = 0; i < mappers.length; i++) {
                TupleElementMapper mapper = mappers[i];
                String attributePath = mapper.getAttributePath();
                if (attributePath != null && hasSubFetches(fetches, attributePath)) {
                    mapper.applyMapping(queryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro, false);
                } else {
                    queryBuilder.select("NULL");
                }
            }
        }
    }

    static boolean hasSubFetches(NavigableSet<String> fetches, String attributePath) {
        // Fetches can never contain a path leading to a view i.e. one for which a dot is allowed to follow.
        // To find a potential match in the fetches, we have to look for an entry that is greater-or-equal to a path
        // See EntityViewConfiguration.getFetches(Collection, ManagedViewTypeImplementor)
        String fetchedPath = fetches.ceiling(attributePath);
        return fetchedPath != null && fetchedPath.startsWith(attributePath) && (fetchedPath.length() == attributePath.length() || fetchedPath.charAt(attributePath.length()) == '.');
    }
}
