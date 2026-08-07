package org.tkit.onecx.human.task.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.human.task.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TaskRestControllerInternal.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ht:all", "ocx-ht:read", "ocx-ht:write", "ocx-ht:delete" })
class TaskRestControllerInternalTest extends AbstractTest {

    private static final String TENANT = "org1";

    @Test
    void getTaskById_shouldReturnTask() {
        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(response.getId()).isEqualTo("11-111");
        assertThat(response.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
        assertThat(response.getProviderTaskId()).isEqualTo("task-1");
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
    void deleteTaskById_shouldDeleteTask() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .delete("/{id}", "44-444")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .get("/{id}", "44-444")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteTaskById_shouldReturn404_whenTaskNotFound() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .delete("/{id}", "missing-id")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptTask_shouldUpdateStatusAndInput() {
        var request = new AcceptTaskRequestDTO(0);
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "22-222")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "22-222")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.ACCEPTED);
        assertThat(updated.getCustomInput()).containsEntry("k1", "v1");
    }

    @Test
    void acceptTask_shouldReturn404_whenTaskNotFound() {
        var request = new AcceptTaskRequestDTO(1);
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "missing-id")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptTask_shouldReturn400_whenModificationCountIsMissing() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body("{}")
                .post("/{id}/accept", "88-888")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void declineTask_shouldUpdateStatusAndInput() {
        var request = new DeclineTaskRequestDTO(0);
        request.setInput(Map.of("reason", "no"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/decline", "33-333")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "33-333")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.DECLINED);
        assertThat(updated.getCustomInput()).containsEntry("reason", "no");
    }

    @Test
    void declineTask_afterAccept_shouldUpdateStatus() {
        var acceptRequest = new AcceptTaskRequestDTO(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(acceptRequest)
                .post("/{id}/accept", "77-777")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "77-777")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.ACCEPTED);

        var declineRequest = new DeclineTaskRequestDTO(updated.getModificationCount());
        declineRequest.setInput(Map.of("reason", "already accepted"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(declineRequest)
                .post("/{id}/decline", "77-777")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "77-777")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.DECLINED);
    }

    @Test
    void searchTasks_shouldReturnFilteredTasks() {
        var criteria = new TaskSearchCriteriaDTO();
        criteria.setProviderTaskId("task-5");
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

    @Test
    void searchTasks_shouldReturn400_whenPageSizeIsTooLarge() {
        var criteria = new TaskSearchCriteriaDTO();
        criteria.setPageNumber(0);
        criteria.setPageSize(1001);

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getInvalidParams()).isNotEmpty();
    }
}
