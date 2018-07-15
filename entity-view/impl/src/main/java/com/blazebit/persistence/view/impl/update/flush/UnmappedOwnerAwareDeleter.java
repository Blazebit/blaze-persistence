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

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class UnmappedOwnerAwareDeleter {

    private final ViewToEntityMapper idMapper;
    private final UnmappedAttributeCascadeDeleter[] routedPreDeleters;
    private final UnmappedAttributeCascadeDeleter[] routedPostDeleters;

    public UnmappedOwnerAwareDeleter(DirtyAttributeFlusher<?, ?, ?> idFlusher, UnmappedAttributeCascadeDeleter[] routedPreDeleters, UnmappedAttributeCascadeDeleter[] routedPostDeleters) {
        this.idMapper = idFlusher instanceof EmbeddableAttributeFlusher<?, ?> ? ((EmbeddableAttributeFlusher) idFlusher).getViewToEntityMapper() : null;
        this.routedPreDeleters = routedPreDeleters;
        this.routedPostDeleters = routedPostDeleters;
    }

    private UnmappedOwnerAwareDeleter(ViewToEntityMapper idMapper, UnmappedAttributeCascadeDeleter[] routedPreDeleters, UnmappedAttributeCascadeDeleter[] routedPostDeleters) {
        this.idMapper = idMapper;
        this.routedPreDeleters = routedPreDeleters;
        this.routedPostDeleters = routedPostDeleters;
    }

    public void preDelete(UpdateContext context, EntityViewProxy view) {
        if (routedPreDeleters != null && routedPreDeleters.length != 0) {
            Object entityId = view.$$_getId();
            if (idMapper != null) {
                entityId = idMapper.applyToEntity(context, null, entityId);
            }
            // Since the embeddable was nulled down, we have to cascade deletes
            for (int i = 0; i < routedPreDeleters.length; i++) {
                routedPreDeleters[i].removeByOwnerId(context, entityId);
            }
        }
    }

    public void postDelete(UpdateContext context, EntityViewProxy view) {
        if (routedPostDeleters != null && routedPostDeleters.length != 0) {
            Object entityId = view.$$_getId();
            if (idMapper != null) {
                entityId = idMapper.applyToEntity(context, null, entityId);
            }
            // Since the embeddable was nulled down, we have to cascade deletes
            for (int i = 0; i < routedPostDeleters.length; i++) {
                routedPostDeleters[i].removeByOwnerId(context, entityId);
            }
        }
    }

    public UnmappedOwnerAwareDeleter getSubDeleter(DirtyAttributeFlusher<?, Object, Object> flusher) {
        if (!(flusher instanceof EmbeddableAttributeFlusher<?, ?>)) {
            return null;
        }
        EmbeddableAttributeFlusher<?, ?> embeddableFlusher = (EmbeddableAttributeFlusher<?, ?>) flusher;
        List<UnmappedAttributeCascadeDeleter> subPreDeleters = new ArrayList<>();
        List<UnmappedAttributeCascadeDeleter> subPostDeleters = new ArrayList<>();
        if (routedPreDeleters != null && routedPreDeleters.length != 0) {
            for (UnmappedAttributeCascadeDeleter routedDeleter : routedPreDeleters) {
                if (routedDeleter.getAttributeValuePath().startsWith(embeddableFlusher.getMapping())) {
                    subPreDeleters.add(routedDeleter);
                }
            }
        }
        if (routedPostDeleters != null && routedPostDeleters.length != 0) {
            for (UnmappedAttributeCascadeDeleter routedDeleter : routedPostDeleters) {
                if (routedDeleter.getAttributeValuePath().startsWith(embeddableFlusher.getMapping())) {
                    subPostDeleters.add(routedDeleter);
                }
            }
        }

        if (subPreDeleters.isEmpty() && subPostDeleters.isEmpty()) {
            return null;
        }
        return new UnmappedOwnerAwareDeleter(
                idMapper,
                subPreDeleters.isEmpty() ? null : subPreDeleters.toArray(new UnmappedAttributeCascadeDeleter[subPreDeleters.size()]),
                subPostDeleters.isEmpty() ? null : subPostDeleters.toArray(new UnmappedAttributeCascadeDeleter[subPostDeleters.size()])
        );
    }
}
