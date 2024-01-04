/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.proxy;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;
import com.blazebit.persistence.view.testsuite.proxy.model.CreatableEmbeddableTestEntityViewWithEmbeddableContainingConstructor;
import com.blazebit.persistence.view.testsuite.proxy.model.EmbeddableTestEntityNestedEmbeddableView;
import com.blazebit.persistence.view.testsuite.proxy.model.EmbeddableTestEntityView;
import com.blazebit.persistence.view.testsuite.proxy.model.NameObjectView;
import com.blazebit.persistence.view.testsuite.proxy.model.UpdatableEmbeddableTestEntityNestedEmbeddableView;
import com.blazebit.persistence.view.testsuite.proxy.model.UpdatableEmbeddableTestEntityView;
import com.blazebit.persistence.view.testsuite.proxy.model.UpdatableNameObjectView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class ProxyFactoryEmeddableTest extends AbstractEntityViewTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                EmbeddableTestEntity.class,
                EmbeddableTestEntityId.class,
                EmbeddableTestEntitySub.class,
                EmbeddableTestEntityEmbeddable.class,
                EmbeddableTestEntityNestedEmbeddable.class,
                IntIdEntity.class,
                NameObject.class
        };
    }

    private EntityViewManager getEntityViewManager() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.setOptionalParameter("test", "String");
        return build(
                cfg,
                EmbeddableTestEntityView.class,
                EmbeddableTestEntityView.Id.class,
                UpdatableEmbeddableTestEntityView.class,
                UpdatableEmbeddableTestEntityView.Id.class,
                UpdatableEmbeddableTestEntityView.ReadOnlyEmbeddableTestEntityEmbeddableView.class,
                UpdatableEmbeddableTestEntityView.EmbeddableTestEntityEmbeddableView.class,
                EmbeddableTestEntityNestedEmbeddableView.class,
                UpdatableEmbeddableTestEntityNestedEmbeddableView.class,
                NameObjectView.class,
                UpdatableNameObjectView.class,
                IntIdEntityView.class
        );
    }

    @Test
    public void testProxyCreateInitialization() {
        Object obj = new Object();
        EntityViewManager evm = getEntityViewManager();
        UpdatableEmbeddableTestEntityView instance = evm.create(UpdatableEmbeddableTestEntityView.class, Collections.singletonMap("test", obj));

        assertNotNull(instance.getId());
        assertNull(instance.getId().getKey());
        assertNull(instance.getId().getValue());
        assertEquals(obj, instance.getTest());
        assertNotNull(instance.getEmbeddable());
        assertNotNull(instance.getEmbeddable().getElementCollection());
        assertNotNull(instance.getEmbeddable().getManyToMany());
        assertNotNull(instance.getEmbeddable().getOneToMany());
        assertNotNull(instance.getEmbeddable().getNestedEmbeddable());
        assertNotNull(instance.getEmbeddable().getNestedEmbeddable().getNestedOneToMany());

        assertNotNull(instance.getEmbeddable().getElementCollection().get("test"));

        assertNotNull(instance.getMyEmbeddable());
        assertNotNull(instance.getMyEmbeddable().getElementCollection());
        assertNotNull(instance.getMyEmbeddable().getManyToMany());
        assertNotNull(instance.getMyEmbeddable().getOneToMany());
        assertNotNull(instance.getMyEmbeddable().getNestedEmbeddable());
        assertNotNull(instance.getMyEmbeddable().getNestedEmbeddable().getNestedOneToMany());
        assertNotNull(instance.getElementCollection4());
    }

    @Test
    public void testCreatableEmbeddableWithEmbeddableContainingConstructor() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.setProperty(ConfigurationProperties.PROXY_EAGER_LOADING, "true");

        try {
            build(
                    cfg,
                    EmbeddableTestEntityView.class,
                    EmbeddableTestEntityView.Id.class,
                    CreatableEmbeddableTestEntityViewWithEmbeddableContainingConstructor.class,
                    CreatableEmbeddableTestEntityViewWithEmbeddableContainingConstructor.ReadOnlyEmbeddableTestEntityEmbeddableView.class
            );
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("empty constructor"));
        }
    }
}
