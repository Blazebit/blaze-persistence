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

package com.blazebit.persistence.view.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AbstractEntityViewTest extends AbstractCoreTest {

    protected EntityViewManager evm;

    protected ViewMetamodel build(Class<?>... classes) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.setProperty(ConfigurationProperties.PROXY_EAGER_LOADING, "true");
        cfg.setProperty(ConfigurationProperties.UPDATER_EAGER_LOADING, "true");
        for (Class<?> c : classes) {
            cfg.addEntityView(c);
        }
        evm = cfg.createEntityViewManager(cbf);
        return evm.getMetamodel();
    }

    protected <T> CriteriaBuilder<T> applySetting(EntityViewManager evm, Class<T> entityViewClass, CriteriaBuilder<?> criteriaBuilder) {
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityViewClass);
        return evm.applySetting(setting, criteriaBuilder);
    }

}
