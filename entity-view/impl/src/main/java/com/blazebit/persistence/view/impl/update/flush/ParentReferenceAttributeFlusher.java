/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ParentReferenceAttributeFlusher<E, V> extends BasicAttributeFlusher<E, V> {

    private final Map<String, String> writableMappings;
    private final Mapper<V, E> mapper;
    private final String[] updateQueryFragments;
    private final Map.Entry<String, AttributeAccessor>[] parameterAccessors;

    public ParentReferenceAttributeFlusher(EntityViewManagerImpl evm, Class<?> parentEntityClass, String attributeName, String mapping, Map<String, String> writableMappings, TypeDescriptor typeDescriptor, AttributeAccessor attributeAccessor, Mapper<V, E> mapper) {
        super(attributeName, mapping, true, false, true, false, false, false, null, typeDescriptor, mapping, mapping, attributeAccessor, null, null, null, null);
        this.writableMappings = writableMappings;
        this.mapper = mapper;
        if (writableMappings != null) {
            List<String> fragments = new ArrayList<>(writableMappings.size() * 2);
            Map<String, AttributeAccessor> accessors = new HashMap<>(writableMappings.size());
            for (Map.Entry<String, String> entry : writableMappings.entrySet()) {
                String s = entry.getValue();
                String parameterName = s.replace('.', '_');
                fragments.add(s);
                fragments.add(parameterName);
                accessors.put(parameterName, Accessors.forEntityMapping(evm, parentEntityClass, entry.getKey()));
            }
            this.updateQueryFragments = fragments.toArray(new String[fragments.size()]);
            this.parameterAccessors = (Map.Entry<String, AttributeAccessor>[]) accessors.entrySet().toArray(new Map.Entry[accessors.size()]);
        } else {
            this.updateQueryFragments = null;
            this.parameterAccessors = null;
        }
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        if (writableMappings != null) {
            if (mappingPrefix == null) {
                sb.append(updateQueryFragments[0]);
                sb.append(" = :");
                sb.append(updateQueryFragments[1]);
                for (int i = 2; i < updateQueryFragments.length; i += 2) {
                    sb.append(separator);
                    sb.append(updateQueryFragments[i]);
                    sb.append(" = :");
                    sb.append(updateQueryFragments[i + 1]);
                }
            } else {
                sb.append(mappingPrefix).append(updateQueryFragments[0]);
                sb.append(" = :");
                sb.append(parameterPrefix).append(updateQueryFragments[1]);
                for (int i = 2; i < updateQueryFragments.length; i += 2) {
                    sb.append(separator);
                    sb.append(mappingPrefix).append(updateQueryFragments[i]);
                    sb.append(" = :");
                    sb.append(parameterPrefix).append(updateQueryFragments[i + 1]);
                }
            }
        } else {
            super.appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator);
        }
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        if (query != null && writableMappings != null) {
            for (int i = 0; i < parameterAccessors.length; i++) {
                Map.Entry<String, AttributeAccessor> parameterAccessor = parameterAccessors[i];
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterAccessor.getKey();
                } else {
                    parameter = parameterPrefix + parameterAccessor.getKey();
                }
                query.setParameter(parameter, parameterAccessor.getValue().getValue(value));
            }
        } else {
            super.flushQuery(context, parameterPrefix, query, view, value, ownerAwareDeleter);
        }
    }

    @Override
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        mapper.map(value, entity);
        return true;
    }
}
