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
 * @since 1.0.0
 */
public class AliasManager {

    private static final int DEFAULT_IMPLICIT_ALIAS_START_IDX = 0;

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
            throw new IllegalArgumentException("Alias '" + alias + "' already exists");
        }
        aliasMap.put(alias, aliasInfo);
        aliasCounterMap.put(alias, DEFAULT_IMPLICIT_ALIAS_START_IDX);
        return alias;
    }

    public String generateRootAlias(String alias) {
        return generatePostfixedAlias(alias, DEFAULT_IMPLICIT_ALIAS_START_IDX);
    }

    // TODO: rewrite tests for join aliases to be 0-based so we can remove this method
    public String generateJoinAlias(String alias) {
        return generatePostfixedAlias(alias, 1);
    }

    private String generatePostfixedAlias(String alias, int startIdx) {
        Integer counter;
        String nonPostfixed = alias;
        if ((counter = getCounterHierarchical(alias)) == null) {
            // alias does not exist so just register it
            counter = startIdx;
        } else {
            // non postfixed version of the alias already exists
            counter++;
        }
        alias = alias + "_" + counter;
        aliasCounterMap.put(nonPostfixed, counter);
        return alias;
    }

    private AliasInfo getHierarchical(String alias) {
        AliasInfo info = aliasMap.get(alias);
        if (info == null && parent != null) {
            info = parent.getHierarchical(alias);
        }
        return info;
    }

    private Integer getCounterHierarchical(String alias) {
        Integer counter = aliasCounterMap.get(alias);
        if (counter == null && parent != null) {
            counter = parent.getCounterHierarchical(alias);
        }
        return counter;
    }

    public void unregisterAliasInfoForBottomLevel(AliasInfo aliasInfo) {
        String alias = aliasInfo.getAlias();
        if (alias != null) {
            aliasMap.remove(alias);
            int counter = aliasCounterMap.get(alias).intValue();

            if (alias.endsWith("_" + counter)) {
                if (counter == DEFAULT_IMPLICIT_ALIAS_START_IDX) {
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
