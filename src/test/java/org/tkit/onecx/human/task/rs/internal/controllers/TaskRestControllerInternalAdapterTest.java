package org.tkit.onecx.human.task.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import jakarta.ws.rs.HttpMethod;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.tkit.onecx.human.task.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.human.task.rs.internal.model.AcceptTaskRequestDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.DeclineTaskRequestDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskStatusDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TaskRestControllerInternal.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ht:all", "ocx-ht:read", "ocx-ht:write",
        "ocx-ht:delete" })
class TaskRestControllerInternalAdapterTest extends AbstractTest {

    private static final String TENANT = "org1";
    private static final String ADAPTER_ACCEPT_PATH = "/v1/tasks/accept";
    private static final String ADAPTER_DECLINE_PATH = "/v1/tasks/decline";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void registerDefaultAdapterMocks() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .withPriority(0).withId(MOCK_ID)
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))
                .withPriority(0).withId(MOCK_ID)
                .respond(HttpResponse.response().withStatusCode(NO_CONTENT.getStatusCode()));
    }

    @AfterEach
    void resetMocks() {
        mockServerClient.clear(MOCK_ID);
    }

    @Test
    void acceptTask_shouldPersist_whenAdapterReturns204() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .withPriority(100).withId(MOCK_ID)
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
    void acceptTask_shouldNotPersist_whenAdapterReturns400() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .withPriority(100).withId(MOCK_ID)
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
    void declineTask_shouldPersist_whenAdapterReturns204() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))
                .withPriority(100).withId(MOCK_ID)
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
    void acceptTask_shouldNotPersist_whenAdapterReturns200() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_ACCEPT_PATH).withMethod(HttpMethod.POST))
                .withPriority(100).withId(MOCK_ID)
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
    void declineTask_shouldNotPersist_whenAdapterReturns200() {
        mockServerClient
                .when(HttpRequest.request().withPath(ADAPTER_DECLINE_PATH).withMethod(HttpMethod.POST))
                .withPriority(100).withId(MOCK_ID)
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
                .withPriority(100).withId(MOCK_ID)
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
}
