/*
 * Copyright 2014 - 2020 Blazebit.
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
package com.blazebit.persistence.examples.quarkus.testsuite.h2;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class H2DatabaseTestResource implements QuarkusTestResourceLifecycleManager {

    private Server tcpServer;

    @Override
    public Map<String, String> start() {

        try {
            tcpServer = Server.createTcpServer();
            tcpServer.start();
            System.out.println("[INFO] H2 database started in TCP server mode; server status: " + tcpServer.getStatus());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyMap();
    }

    @Override
    public synchronized void stop() {
        if (tcpServer != null) {
            tcpServer.stop();
            System.out.println("[INFO] H2 database was shut down; server status: " + tcpServer.getStatus());
            tcpServer = null;
        }
    }
}
