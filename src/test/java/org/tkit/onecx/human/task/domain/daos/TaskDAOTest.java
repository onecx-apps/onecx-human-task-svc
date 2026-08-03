package org.tkit.onecx.human.task.domain.daos;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.onecx.human.task.domain.criteria.TaskSearchCriteria;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;

@QuarkusTest
class TaskDAOTest {

    @Inject
    TaskDAO taskDAO;

    @InjectSpy
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        entityManager.createQuery("delete from Task").executeUpdate();
    }

    @Test
    @Transactional
    void createTask() {
        var task = task("1", "Title1", Task.Status.CREATED, "CAMUNDA");
        taskDAO.create(task);

        Assertions.assertNotNull(task.getId());

        var result = taskDAO.findById(task.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Title1", result.getTitle());
        Assertions.assertEquals("CAMUNDA", result.getProviderType());
        Assertions.assertEquals("1", result.getProviderTaskId());
        Assertions.assertEquals(Task.Status.CREATED, result.getStatus());
        Assertions.assertEquals("https://" + result.getProviderType() + "/" + result.getProviderTaskId(),
                result.getProviderURL());
        Assertions.assertNull(result.getDescription());
    }

    @Test
    @Transactional
    void findById_shouldReturnNull_whenTaskDoesNotExist() {
        Assertions.assertNull(taskDAO.findById("not-existing-id"));
    }

    @Test
    @Transactional
    void customInput_shouldRoundTripAsJson() {
        var task = task("dao-json-1", "Json Task", Task.Status.CREATED, "N8N");
        task.setCustomInput(Map.of("key1", "value1", "key2", "value2"));
        taskDAO.create(task);

        var result = taskDAO.findById(task.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Map.of("key1", "value1", "key2", "value2"), result.getCustomInput());
    }

    @Test
    @Transactional
    void findTasksByCriteria_filterByTitle() {
        taskDAO.create(task("1", "Title1", Task.Status.CREATED, "N8N"));
        taskDAO.create(task("2", "Title2", Task.Status.ACCEPTED, "CAMUNDA"));

        var criteria = new TaskSearchCriteria();
        criteria.setTitle("Title2");
        criteria.setPageNumber(0);
        criteria.setPageSize(10);

        var result = taskDAO.findTasksByCriteria(criteria);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Title2", result.getStream().findFirst().orElseThrow().getTitle());

        criteria.setTitle("nonexistent-title");
        Assertions.assertEquals(0, taskDAO.findTasksByCriteria(criteria).getTotalElements());
    }

    @Test
    @Transactional
    void findTasksByCriteria_filterByStatusesAndProviderType() {
        taskDAO.create(task("1", "Title1", Task.Status.CREATED, "N8N"));
        taskDAO.create(task("2", "Title2", Task.Status.ACCEPTED, "N8N"));
        taskDAO.create(task("3", "Title3", Task.Status.CREATED, "CAMUNDA"));

        var criteria = new TaskSearchCriteria();
        criteria.setStatuses(List.of(Task.Status.CREATED));
        criteria.setProviderType("N8N");
        criteria.setPageNumber(0);
        criteria.setPageSize(10);

        var result = taskDAO.findTasksByCriteria(criteria);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Title1", result.getStream().findFirst().orElseThrow().getTitle());
    }

    @Test
    @Transactional
    void findTasksByCriteria_withPagination() {
        taskDAO.create(task("1", "Title2", Task.Status.CREATED, "N8N"));
        taskDAO.create(task("2", "Title2", Task.Status.CREATED, "N8N"));
        taskDAO.create(task("3", "Title3", Task.Status.CREATED, "N8N"));

        var criteria = new TaskSearchCriteria();
        criteria.setPageNumber(0);
        criteria.setPageSize(2);

        var firstPage = taskDAO.findTasksByCriteria(criteria);
        Assertions.assertNotNull(firstPage);
        Assertions.assertEquals(3, firstPage.getTotalElements());
        Assertions.assertEquals(2, firstPage.getStream().toList().size());
        Assertions.assertEquals(2, firstPage.getTotalPages());
        Assertions.assertEquals(0, firstPage.getNumber());
        Assertions.assertEquals(2, firstPage.getSize());

        criteria.setPageNumber(1);
        var secondPage = taskDAO.findTasksByCriteria(criteria);
        Assertions.assertEquals(1, secondPage.getStream().toList().size());
        Assertions.assertEquals(1, secondPage.getNumber());
    }

    @Test
    void methodExceptionTests() {
        Mockito.doThrow(new RuntimeException("Test technical error exception"))
                .when(entityManager).getCriteriaBuilder();
        methodExceptionTests(() -> taskDAO.findById("some-id"),
                TaskDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> taskDAO.findTasksByCriteria(new TaskSearchCriteria()),
                TaskDAO.ErrorKeys.ERROR_FIND_TASK_BY_CRITERIA);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }

    private Task task(String providerTaskId, String title, Task.Status status, String providerType) {
        var task = new Task();
        task.setProviderTaskId(providerTaskId);
        task.setTitle(title);
        task.setStatus(status);
        task.setProviderType(providerType);
        task.setProviderURL("https://" + providerType + "/" + providerTaskId);
        return task;
    }

}
