package org.tkit.onecx.human.task.rs.internal.controllers;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.human.task.domain.daos.TaskDAO;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.rs.internal.mappers.TaskMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.human.task.rs.internal.TasksInternalApi;
import gen.org.tkit.onecx.human.task.rs.internal.model.AcceptTaskRequestDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.DeclineTaskRequestDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskSearchCriteriaDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class TaskRestControllerInternal implements TasksInternalApi {

    @Inject
    TaskDAO dao;

    @Inject
    TaskMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response acceptTask(String id, AcceptTaskRequestDTO acceptTaskRequestDTO) {
        return updateTaskStatus(id, acceptTaskRequestDTO.getModificationCount(), acceptTaskRequestDTO.getInput(),
                Task.Status.ACCEPTED);
    }

    @Override
    public Response declineTask(String id, DeclineTaskRequestDTO declineTaskRequestDTO) {
        return updateTaskStatus(id, declineTaskRequestDTO.getModificationCount(), declineTaskRequestDTO.getInput(),
                Task.Status.DECLINED);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public Response deleteTaskById(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }
        dao.delete(item);
        return Response.noContent().build();
    }

    @Override
    public Response getTaskById(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }
        return Response.ok(mapper.toTaskDTO(item)).build();
    }

    @Override
    public Response searchTasksByCriteria(TaskSearchCriteriaDTO taskSearchCriteriaDTO) {
        var result = dao.findTasksByCriteria(mapper.toTaskSearchCriteria(taskSearchCriteriaDTO));
        return Response.ok(mapper.map(result)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    private Response updateTaskStatus(String id, Integer modificationCount, Map<String, String> input,
            Task.Status status) {
        var item = dao.findById(id);
        if (item == null || item.getStatus() != Task.Status.CREATED
                || !Objects.equals(item.getModificationCount(), modificationCount)) {
            return Response.status(NOT_FOUND).build();
        }

        item.setStatus(status);
        item.setCustomInput(input);
        dao.update(item);
        return Response.noContent().build();
    }
}
