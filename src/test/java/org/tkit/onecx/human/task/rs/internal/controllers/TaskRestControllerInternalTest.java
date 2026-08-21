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

import jakarta.ws.rs.HttpMethod;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.human.task.rs.internal.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TaskRestControllerInternal.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ht:all", "ocx-ht:read", "ocx-ht:write",
        "ocx-ht:delete" })
class TaskRestControllerInternalTest extends AbstractTest {

    private static final String TENANT = "org1";
    private static final String ADAPTER_ACCEPT_PATH = "/v1/tasks/accept";
    private static final String ADAPTER_DECLINE_PATH = "/v1/tasks/decline";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @AfterEach
    void clearMocks() {
        mockServerClient.clear(HttpRequest.request());
    }

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
                .delete("/{id}", "11-111")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .get("/{id}", "11-111")
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
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));

        var request = new AcceptTaskRequestDTO(0);
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
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
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void acceptTask_shouldReturn400_whenModificationCountIsStale() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(new AcceptTaskRequestDTO(5))
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ExceptionMapper.ErrorKeys.OPTIMISTIC_LOCK.name());
    }

    @Test
    void acceptTask_shouldReturn404_whenTaskStatusIsNotCreated() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(new AcceptTaskRequestDTO(0))
                .post("/{id}/accept", "22-222")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptTask_shouldNotPersist_whenAdapterReturns400() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))

                .respond(HttpResponse.response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var request = new AcceptTaskRequestDTO(0);
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
    }

    @Test
    void acceptTask_shouldNotPersist_whenAdapterReturns200() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))

                .respond(HttpResponse.response().withStatusCode(OK.getStatusCode()));

        var request = new AcceptTaskRequestDTO(0);
        request.setInput(Map.of("k1", "v1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(OK.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
    }

    @Test
    void acceptTask_shouldReturn400_whenNoAdapterClient() {
        var request = new AcceptTaskRequestDTO(0);
        request.setInput(Map.of("k1", "v1"));

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/accept", "33-333")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .as(ProblemDetailResponseDTO.class);

        assertThat(response.getErrorCode()).isEqualTo("INVALID_PROVIDER");
    }

    @Test
    void declineTask_shouldUpdateStatusAndInput() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));

        var request = new DeclineTaskRequestDTO(0);
        request.setInput(Map.of("reason", "no"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/decline", "11-111")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.DECLINED);
        assertThat(updated.getCustomInput()).containsEntry("reason", "no");
    }

    @Test
    void declineTask_afterAccept_shouldReturn404() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(new AcceptTaskRequestDTO(0))
                .post("/{id}/accept", "11-111")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
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
                .post("/{id}/decline", "11-111")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void declineTask_shouldReturn404_whenTaskStatusIsNotCreated() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(new DeclineTaskRequestDTO(0))
                .post("/{id}/decline", "22-222")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void declineTask_shouldNotPersist_whenAdapterReturns200() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))

                .respond(HttpResponse.response().withStatusCode(OK.getStatusCode()));

        var request = new DeclineTaskRequestDTO(0);
        request.setInput(Map.of("reason", "no"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/decline", "11-111")
                .then()
                .statusCode(OK.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
    }

    @Test
    void declineTask_shouldNotPersist_whenAdapterReturns400() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))

                .respond(HttpResponse.response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var request = new DeclineTaskRequestDTO(0);
        request.setInput(Map.of("reason", "no"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/{id}/decline", "11-111")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        var updated = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken(TENANT))
                .contentType(APPLICATION_JSON)
                .get("/{id}", "11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .as(TaskDTO.class);

        assertThat(updated.getStatus()).isEqualTo(TaskStatusDTO.CREATED);
    }

    @Test
    void searchTasks_shouldReturnFilteredTasks() {
        var criteria = new TaskSearchCriteriaDTO();
        criteria.setProviderTaskId("task-1");
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
