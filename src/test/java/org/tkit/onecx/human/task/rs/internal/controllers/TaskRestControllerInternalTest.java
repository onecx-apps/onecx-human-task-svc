package org.tkit.onecx.human.task.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.human.task.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.tkit.onecx.human.task.rs.external.v1.model.CreateTaskRequestDTOV1;
import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProviderTypeDTOV1;
import gen.org.tkit.onecx.human.task.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@TestHTTPEndpoint(TaskRestControllerInternal.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ht:all", "ocx-ht:read", "ocx-ht:write", "ocx-ht:delete" })
class TaskRestControllerInternalTest extends AbstractTest {

    private static final String TENANT = "default";

    @BeforeEach
    void cleanDatabase() {
        Arrays.stream(TaskStatusDTO.values()).forEach(this::deleteTasksByStatus);
    }

    @Test
    void getTaskById_shouldReturnTask() {
        var task = createTask("provider-1", ProviderTypeDTO.N8_N.toString());

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", task.getId())
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(response.getId()).isEqualTo(task.getId());
        assertThat(response.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
        assertThat(response.getProviderTaskId()).isEqualTo("provider-1");
    }

    @Test
    void getTaskById_shouldReturn404_whenTaskNotFound() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "missing-id")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptTask_shouldUpdateStatusAndInput() {
        var task = createTask("provider-2", "CAMUNDA");
        var request = new AcceptTaskRequestDTO(task.getModificationCount());
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", task.getId())
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", task.getId())
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.ACCEPTED);
        assertThat(updated.getCustomInput()).containsEntry("k1", "v1");
    }

    @Test
    void declineTask_shouldReturn404_whenModificationCountMismatch() {
        var task = createTask("provider-3", "N8N");
        var request = new DeclineTaskRequestDTO(task.getModificationCount() + 1);
        request.setInput(Map.of("reason", "no"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/decline", task.getId())
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteTask_shouldDeleteTask() {
        var task = createTask("provider-4", "N8N");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .delete("/{id}", task.getId())
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .get("/{id}", task.getId())
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void searchTasks_shouldReturnFilteredTasks() {
        createTask("provider-5", "N8N");
        var acceptedTask = createTask("provider-6", "CAMUNDA");
        var acceptRequest = new AcceptTaskRequestDTO(acceptedTask.getModificationCount());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(acceptRequest)
                .post("/{id}/accept", acceptedTask.getId())
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new TaskSearchCriteriaDTO();
        criteria.setStatuses(List.of(TaskStatusDTO.CREATED));
        criteria.setPageNumber(0);
        criteria.setPageSize(10);

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskPageResultDTO.class);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getStream()).hasSize(1);
        assertThat(response.getStream().getFirst().getStatus()).isEqualTo(TaskStatusDTO.CREATED);
    }

    private TaskDTO createTask(String providerTaskId, String providerType) {
        var request = new CreateTaskRequestDTOV1();
        request.setTitle("Internal Task " + providerTaskId);
        request.setDescription("desc " + providerTaskId);
        request.setProviderTaskId(providerTaskId);
        request.setProviderType(ProviderTypeDTOV1.fromValue(providerType));
        request.setProviderURL("https://" + providerType + "/" + providerTaskId);
        var createUrl = "http://localhost:" + RestAssured.port + "/v1/tasks";

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post(createUrl)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new TaskSearchCriteriaDTO();
        criteria.setProviderTaskId(providerTaskId);
        criteria.setStatuses(List.of(TaskStatusDTO.CREATED));
        criteria.setPageNumber(0);
        criteria.setPageSize(10);

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskPageResultDTO.class);

        assertThat(response.getStream()).isNotEmpty();
        return response.getStream().getFirst();
    }

    private void deleteTasksByStatus(TaskStatusDTO status) {
        var criteria = new TaskSearchCriteriaDTO();
        criteria.setStatuses(List.of(status));
        criteria.setPageNumber(0);
        criteria.setPageSize(1000);

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskPageResultDTO.class);

        response.getStream().forEach(task -> given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .delete("/{id}", task.getId())
                .then()
                .statusCode(NO_CONTENT.getStatusCode()));
    }
}
