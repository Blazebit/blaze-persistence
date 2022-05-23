/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewListeners;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.util.Collection;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewJandexScavenger {
    private static final DotName ENTITY_VIEW = DotName.createSimple(EntityView.class.getName());
    private static final DotName ENTITY_VIEW_LISTENER = DotName.createSimple(EntityViewListener.class.getName());
    private static final DotName ENTITY_VIEW_LISTENERS = DotName.createSimple(EntityViewListeners.class.getName());

    private final IndexView indexView;

    public EntityViewJandexScavenger(IndexView indexView) {
        this.indexView = indexView;
    }

    public EntityViewsBuildItem discoverAndRegisterEntityViews() {
        EntityViewsBuildItem entityViewsBuildItem = new EntityViewsBuildItem();

        Collection<AnnotationInstance> entityViewAnnotations = indexView.getAnnotations(ENTITY_VIEW);

        if (entityViewAnnotations != null) {
            for (AnnotationInstance annotation : entityViewAnnotations) {
                ClassInfo klass = annotation.target().asClass();
                entityViewsBuildItem.addEntityViewClass(klass.name().toString());
            }
        }

        return entityViewsBuildItem;
    }

    public EntityViewListenersBuildItem discoverAndRegisterEntityViewListeners() {
        EntityViewListenersBuildItem entityViewListenersBuildItem = new EntityViewListenersBuildItem();

        Collection<AnnotationInstance> entityViewListenerAnnotations = indexView.getAnnotations(ENTITY_VIEW_LISTENER);

        if (entityViewListenerAnnotations != null) {
            for (AnnotationInstance annotation : entityViewListenerAnnotations) {
                ClassInfo klass = annotation.target().asClass();
                entityViewListenersBuildItem.addEntityViewListenerClass(klass.name().toString());
            }
        }

        Collection<AnnotationInstance> entityViewListenersAnnotations = indexView.getAnnotations(ENTITY_VIEW_LISTENERS);

        if (entityViewListenersAnnotations != null) {
            for (AnnotationInstance annotation : entityViewListenersAnnotations) {
                ClassInfo klass = annotation.target().asClass();
                entityViewListenersBuildItem.addEntityViewListenerClass(klass.name().toString());
            }
        }

        return entityViewListenersBuildItem;
    }
}
