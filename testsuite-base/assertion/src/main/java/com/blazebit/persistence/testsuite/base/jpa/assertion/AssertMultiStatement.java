/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import org.opentest4j.MultipleFailuresError;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertMultiStatement implements AssertStatement {

    private final List<AssertStatement> statements;
    private final List<Throwable> failures = new ArrayList<>();
    private int validations;

    public AssertMultiStatement(List<AssertStatement> statements) {
        this.statements = statements;
        this.validations = statements.size();
    }

    @Override
    public void validate(String query) {
        validations--;
        List<Throwable> tryFailures = new ArrayList<>();
        boolean failed = true;
        for (int i = 0; i < statements.size(); i++) {
            try {
                statements.get(i).validate(query);
                statements.remove(i);
                failed = false;
                break;
            } catch (Throwable t) {
                tryFailures.add(t);
            }
        }
        if (failed) {
            failures.addAll(tryFailures);
        }

        if (validations == 0 && !failures.isEmpty()) {
            throw new MultipleFailuresError("Multiple query validations failed", failures);
        }
    }

}
