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

package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class AliasManager {

    private static final int DEFAULT_IMPLICIT_ALIAS_START_IDX = 0;

    private final AliasManager parent;
    private final Map<String, AliasInfo> aliasMap = new HashMap<String, AliasInfo>(); // maps alias to absolute path and join manager of the declaring query
    private final Map<String, Integer> aliasCounterMap = new HashMap<String, Integer>(); // maps non postfixed aliases to alias counter
    private final List<AliasManager> children = new ArrayList<>();
    private String forbiddenAlias;

    public AliasManager() {
        this.parent = null;
    }

    public AliasManager(AliasManager parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public AliasManager getParent() {
        return parent;
    }

    public boolean isAliasAvailable(String alias) {
        if (getHierarchical(alias) == null) {
            return getChildAlias(alias) == null;
        }
        return false;
    }

    private Integer getCounter(String alias) {
        Integer counter = getCounterHierarchical(alias);
        return counter == null ? getChildCounter(alias) : counter;
    }

    private AliasInfo getChildAlias(String alias) {
        AliasInfo info;
        for (int i = 0; i < children.size(); i++) {
            AliasManager child = children.get(i);
            info = child.getAliasInfoForBottomLevel(alias);
            if (info != null) {
                return info;
            }
            info = child.getChildAlias(alias);
            if (info != null) {
                return info;
            }
        }

        return null;
    }

    private Integer getChildCounter(String alias) {
        Integer info;
        for (int i = 0; i < children.size(); i++) {
            AliasManager child = children.get(i);
            info = child.aliasCounterMap.get(alias);
            if (info != null) {
                return info;
            }
            info = child.getChildCounter(alias);
            if (info != null) {
                return info;
            }
        }

        return null;
    }

    public AliasInfo getAliasInfo(String alias) {
        AliasInfo aliasInfo = getHierarchical(alias);
        if (alias.equals(forbiddenAlias)) {
            throw new IllegalArgumentException("Usage of alias '" + alias + "' is forbidden!");
        }
        return aliasInfo;
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
        if (!isAliasAvailable(alias)) {
            throw new IllegalArgumentException("Alias '" + alias + "' already exists");
        }
        aliasMap.put(alias, aliasInfo);
        aliasCounterMap.put(alias, DEFAULT_IMPLICIT_ALIAS_START_IDX);
        return alias;
    }

    void registerAliasInfoOnly(AliasInfo aliasInfo) {
        aliasMap.put(aliasInfo.getAlias(), aliasInfo);
    }

    public String generateRootAlias(String alias) {
        return generatePostfixedAlias(alias, DEFAULT_IMPLICIT_ALIAS_START_IDX, AliasGenerationMode.CREATE);
    }

    // TODO: rewrite tests for join aliases to be 0-based so we can remove this method
    public String generateJoinAlias(String alias) {
        return generatePostfixedAlias(alias, 1, AliasGenerationMode.CREATE);
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.0
     */
    private enum AliasGenerationMode {
        CREATE,
        FIND_PARENT,
        FIND_CHILD
    }

    String generatePostfixedAlias(String baseAlias, int startIdx, AliasGenerationMode generationMode) {
        // Don't copy counter from parent but implement this in the alias manager that owns the alias,
        // otherwise we could run into collisions
        String newAlias;
        Integer counter = aliasCounterMap.get(baseAlias);
        if (counter == null) {
            if (generationMode != AliasGenerationMode.FIND_CHILD && parent != null) {
                newAlias = parent.generatePostfixedAlias(baseAlias, startIdx, AliasGenerationMode.FIND_PARENT);
                if (newAlias != null) {
                    return newAlias;
                }
            }
            if (generationMode != AliasGenerationMode.FIND_PARENT) {
                for (int i = 0; i < children.size(); i++) {
                    AliasManager child = children.get(i);
                    newAlias = child.generatePostfixedAlias(baseAlias, startIdx, AliasGenerationMode.FIND_CHILD);
                    if (newAlias != null) {
                        return newAlias;
                    }
                }
                if (generationMode == AliasGenerationMode.CREATE) {
                    counter = startIdx;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            counter++;
        }
        newAlias = baseAlias + "_" + counter;
        aliasCounterMap.put(baseAlias, counter);
        return newAlias;
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
        return counter == null ? getChildCounter(alias) : counter;
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

    public String getForbiddenAlias() {
        return forbiddenAlias;
    }

    public void setForbiddenAlias(String forbiddenAlias) {
        this.forbiddenAlias = forbiddenAlias;
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
