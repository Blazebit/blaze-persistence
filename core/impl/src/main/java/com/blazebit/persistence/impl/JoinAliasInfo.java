/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinAliasInfo implements AliasInfo {

    private String alias;
    private JoinNode joinNode;
    private boolean implicit;
    private final boolean rootNode;
    // The absolute normalized path with the root as implicit base
    private final String absolutePath;
    private final AliasManager aliasOwner;

    public JoinAliasInfo(String alias, String absolutePath, boolean implicit, boolean rootNode, AliasManager aliasOwner) {
        this.alias = alias;
        this.absolutePath = absolutePath;
        this.implicit = implicit;
        this.rootNode = rootNode;
        this.aliasOwner = aliasOwner;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public JoinNode getJoinNode() {
        return joinNode;
    }

    public void setJoinNode(JoinNode joinNode) {
        this.joinNode = joinNode;
    }

    public String getAbsolutePath() {
        if (implicit) {
            return absolutePath;
        } else {
            return alias;
        }
    }

    public void render(StringBuilder sb) {
        sb.append(alias);
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public boolean isRootNode() {
        return rootNode;
    }

    @Override
    public AliasManager getAliasOwner() {
        return this.aliasOwner;
    }
}
