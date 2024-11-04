/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.keyset;

import java.io.Serializable;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class KeysetPaginationHelper {

    private KeysetPaginationHelper() {
    }

    public static Serializable[] extractKey(Object[] tuple, int[] mapping, int suffixLength) {
        Serializable[] key = new Serializable[mapping.length];
        int suffixIndex = tuple.length - suffixLength;
        for (int i = 0; i < mapping.length; i++) {
            int index = mapping[i];
            if (index == -1) {
                key[i] = (Serializable) tuple[suffixIndex++];
            } else {
                key[i] = (Serializable) tuple[index];
            }
        }
        return key;
    }

    public static KeysetMode getKeysetMode(KeysetPage keysetPage, Object entityId, int firstRow, int pageSize) {
        // a keyset must be given
        if (keysetPage == null) {
            return KeysetMode.NONE;
        }
        // We do offset pagination to scroll to the page where an entity is
        if (entityId != null) {
            return KeysetMode.NONE;
        }

        // for the first page we fall back to offset pagination to prevent newly inserted elements from
        // * not being show at all
        // * block previously shown elements from being returned in the result list
        if (firstRow == 0) {
            return KeysetMode.NONE;
        }

        int offset = keysetPage.getFirstResult() - firstRow;
        int keysetPageSize = keysetPage.getMaxResults();

        if (offset > 0 && offset <= keysetPageSize) {
            // We went to the previous page
            if (isValidKey(keysetPage.getLowest())) {
                return KeysetMode.PREVIOUS;
            } else {
                return KeysetMode.NONE;
            }
        } else if (offset < 0 && -offset <= keysetPageSize) {
            // We went to the next page
            if (isValidKey(keysetPage.getHighest())) {
                return KeysetMode.NEXT;
            } else {
                return KeysetMode.NONE;
            }
        } else if (offset == 0) {
            // Same page again
            if (isValidKey(keysetPage.getLowest())) {
                return KeysetMode.SAME;
            } else {
                return KeysetMode.NONE;
            }
        } else {
            // The last key set is away more than one page
            return KeysetMode.NONE;
        }
    }

    private static boolean isValidKey(Keyset keyset) {
        if (keyset == null) {
            return false;
        }

        Serializable[] key = keyset.getTuple();
        return key != null && key.length > 0;
    }
}
