package org.tkit.onecx.human.task.rs.internal.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.quarkus.jpa.daos.PageResult;

import gen.org.tkit.onecx.human.task.rs.internal.model.ProviderTypeDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskSearchCriteriaDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskStatusDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TaskMapperTest {

    @Inject
    TaskMapper mapper;

    @Test
    void toTaskDTO_shouldMapFieldsAndEnums() {
        var task = new Task();
        task.setTitle("Task title");
        task.setDescription("Task desc");
        task.setStatus(Task.Status.ACCEPTED);
        task.setProviderType("N8N");
        task.setProviderTaskId("provider-1");
        task.setProviderURL("https://n8n/provider-1");
        task.setCustomInput(Map.of("a", "b"));

        var result = mapper.toTaskDTO(task);

        assertNotNull(result);
        assertEquals("Task title", result.getTitle());
        assertEquals("Task desc", result.getDescription());
        assertEquals(TaskStatusDTO.ACCEPTED, result.getStatus());
        assertEquals(ProviderTypeDTO.N8_N, result.getProviderType());
        assertEquals("provider-1", result.getProviderTaskId());
        assertEquals("https://n8n/provider-1", result.getProviderURL());
        assertEquals("b", result.getCustomInput().get("a"));
    }

    @Test
    void toTaskSearchCriteria_shouldMapStatusesAndProviderType() {
        var dto = new TaskSearchCriteriaDTO();
        dto.setStatuses(List.of(TaskStatusDTO.CREATED, TaskStatusDTO.DECLINED));
        dto.setProviderType(ProviderTypeDTO.N8_N);
        dto.setPageNumber(3);
        dto.setPageSize(25);

        var result = mapper.toTaskSearchCriteria(dto);

        assertNotNull(result);
        assertEquals(List.of(Task.Status.CREATED, Task.Status.DECLINED), result.getStatuses());
        assertEquals("N8N", result.getProviderType());
        assertEquals(3, result.getPageNumber());
        assertEquals(25, result.getPageSize());
    }

    @SuppressWarnings("unchecked")
    @Test
    void mapPage_shouldMapPaginationAndContent() {
        var task = new Task();
        task.setStatus(Task.Status.CREATED);
        task.setProviderType("CAMUNDA");
        task.setProviderTaskId("provider-2");

        PageResult<Task> page = Mockito.mock(PageResult.class);
        Mockito.when(page.getTotalElements()).thenReturn(1L);
        Mockito.when(page.getNumber()).thenReturn(0L);
        Mockito.when(page.getSize()).thenReturn(10L);
        Mockito.when(page.getTotalPages()).thenReturn(1L);
        Mockito.when(page.getStream()).thenReturn(Stream.of(task));

        var result = mapper.map(page);

        assertNotNull(result);
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(1L, result.getTotalPages());
        assertEquals(1, result.getStream().size());
        assertEquals(TaskStatusDTO.CREATED, result.getStream().getFirst().getStatus());
        assertEquals(ProviderTypeDTO.CAMUNDA, result.getStream().getFirst().getProviderType());
    }
}
