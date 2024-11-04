/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.spring.data.testsuite.webmvc.controller;

import com.blazebit.persistence.view.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Void> handleUsernameAlreadyUsedException() {
        return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
    }
}
