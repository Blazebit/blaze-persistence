/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.config;

import ${package}.repository.CatSimpleViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

    @Autowired
    CatSimpleViewRepository catRepo;

    @GetMapping("/")
    @ResponseBody
    String home() {
        return catRepo.findAll().toString();
    }

}