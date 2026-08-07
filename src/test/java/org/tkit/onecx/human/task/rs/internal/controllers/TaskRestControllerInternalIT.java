package org.tkit.onecx.human.task.rs.internal.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@TestHTTPEndpoint(TaskRestControllerInternal.class)
@SuppressWarnings("java:S2187")
class TaskRestControllerInternalIT extends TaskRestControllerInternalTest {
}
