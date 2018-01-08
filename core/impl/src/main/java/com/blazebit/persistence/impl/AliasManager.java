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

package com.blazebit.persistence.impl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class AliasManager {

    private final AliasManager parent;
    private final Map<String, AliasInfo> aliasMap = new HashMap<String, AliasInfo>(); // maps alias to absolute path and join manager of
                                                                                      // the declaring query
    private final Map<String, Integer> aliasCounterMap = new HashMap<String, Integer>(); // maps non postfixed aliases to alias counter

    public AliasManager() {
        this.parent = null;
    }

    public AliasManager(AliasManager parent) {
        this.parent = parent;
    }

    public AliasManager getParent() {
        return parent;
    }

    public AliasInfo getAliasInfo(String alias) {
        return getHierarchical(alias);
    }

    public AliasInfo getAliasInfoForBottomLevel(String alias) {
        return aliasMap.get(alias);
    }

    public void applyFrom(AliasManager aliasManager) {
        aliasCounterMap.putAll(aliasManager.aliasCounterMap);
    }

    /**
     * Register the given alias info if possible
     * If the given alias already exists an exception is thrown.
     *
     * @param aliasInfo
     * @return The registered alias
     */
    public String registerAliasInfo(AliasInfo aliasInfo) {
        String alias = aliasInfo.getAlias();
        if (getHierarchical(alias) != null) {
            throw new IllegalArgumentException("Alias '" + alias + "' already exsits");
        }
        aliasMap.put(alias, aliasInfo);
        aliasCounterMap.put(alias, 0);
        return alias;
    }

    public String generatePostfixedAlias(String alias) {
        Integer counter;
        String nonPostfixed = alias;
        if ((counter = getCounterHierarchical(alias)) != null) {
            // non postfixed version of the alias already exists
            counter++;
        } else {
            // alias does not exist so just register it
            counter = 1;
        }
        alias = alias + "_" + counter;
        aliasCounterMap.put(nonPostfixed, counter);
        return alias;
    }

    private AliasInfo getHierarchical(String alias) {
        AliasInfo info = null;
        if (parent != null) {
            info = parent.getHierarchical(alias);
        }
        if (info == null) {
            info = aliasMap.get(alias);
        }
        return info;
    }

    private Integer getCounterHierarchical(String alias) {
        Integer counter = null;
        if (parent != null) {
            counter = parent.getCounterHierarchical(alias);
        }
        if (counter == null) {
            counter = aliasCounterMap.get(alias);
        }
        return counter;
    }

    public void unregisterAliasInfoForBottomLevel(AliasInfo aliasInfo) {
        String alias = aliasInfo.getAlias();
        if (alias != null) {
            aliasMap.remove(alias);
            int counter = aliasCounterMap.get(alias).intValue();

            if (alias.endsWith("_" + counter)) {
                if (counter == 1) {
                    aliasCounterMap.remove(alias);
                } else {
                    aliasCounterMap.put(alias, counter - 1);
                }
            }
        }
    }

    public Map<String, AliasInfo> getAliasMapForBottomLevel() {
        return aliasMap;
    }

    public boolean isSelectAlias(String alias) {
        AliasInfo info;
        if ((info = aliasMap.get(alias)) != null) {
            return info instanceof SelectInfo;
        }
        return false;
    }
}
