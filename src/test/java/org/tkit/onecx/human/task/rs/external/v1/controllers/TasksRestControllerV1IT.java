package org.tkit.onecx.human.task.rs.external.v1.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@TestHTTPEndpoint(TasksRestControllerV1.class)
@SuppressWarnings("java:S2187")
class TasksRestControllerV1IT extends TasksRestControllerV1Test {
}
