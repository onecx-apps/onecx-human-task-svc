package org.tkit.onecx.human.task.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.human.task.rs.external.v1.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.tkit.onecx.human.task.rs.external.v1.model.CreateTaskRequestDTOV1;
import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProblemDetailResponseDTOV1;
import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProviderTypeDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TasksRestControllerV1.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ht:all", "ocx-ht:read", "ocx-ht:write",
        "ocx-ht:delete" })
class TasksRestControllerV1Test extends AbstractTest {

    @Test
    void createTask() {
        var request = new CreateTaskRequestDTOV1();
        request.setTitle("Test task");
        request.setDescription("Task description");
        request.setProviderType(ProviderTypeDTOV1.N8_N);
        request.setProviderTaskId("task-1");
        request.setProviderURL("https://n8n.example.com/callback");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void createTask_shouldReturn400_whenRequiredFieldsAreMissing() {
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(new CreateTaskRequestDTOV1())
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ProblemDetailResponseDTOV1.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getInvalidParams()).isNotEmpty();
    }
}
