/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import java.io.PrintWriter;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class PrintWriterCloser implements Runnable {
    private final PrintWriter pw;

    public PrintWriterCloser(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    public void run() {
        pw.close();
    }
}
