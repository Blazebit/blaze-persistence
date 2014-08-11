/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.BaseQueryBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinAliasInfo implements AliasInfo {

    private String alias;
    // The absolute normalized path with the root as implicit base
    private final String absolutePath;
    private boolean implicit;
    private final BaseQueryBuilder<?, ?> aliasOwner;

    public JoinAliasInfo(String alias, String absolutePath, boolean implicit, BaseQueryBuilder<?, ?> aliasOwner) {
        this.alias = alias;
        this.absolutePath = absolutePath;
        this.implicit = implicit;
        this.aliasOwner = aliasOwner;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    @Override
    public BaseQueryBuilder<?, ?> getAliasOwner() {
        return this.aliasOwner;
    }
}
