package org.tkit.onecx.human.task.rs.internal.controllers;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.human.task.domain.daos.TaskDAO;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.rs.internal.mappers.TaskMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
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
    public Response getTaskById(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }
        return Response.ok(mapper.toTaskDTO(item)).build();
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
    public Response acceptTask(String id, AcceptTaskRequestDTO acceptTaskRequestDTO) {
        return updateTaskStatus(id, acceptTaskRequestDTO.getInput(),
                Task.Status.ACCEPTED);
    }

    @Override
    public Response declineTask(String id, DeclineTaskRequestDTO declineTaskRequestDTO) {
        return updateTaskStatus(id, declineTaskRequestDTO.getInput(),
                Task.Status.DECLINED);
    }

    @Override
    public Response searchTasksByCriteria(TaskSearchCriteriaDTO taskSearchCriteriaDTO) {
        var result = dao.findTasksByCriteria(mapper.toTaskSearchCriteria(taskSearchCriteriaDTO));
        return Response.ok(mapper.totaskPageResultDTO(result)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> optimisticLockException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

    private Response updateTaskStatus(String id, Map<String, String> input,
            Task.Status status) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }

        item.setStatus(status);
        item.setCustomInput(input);
        dao.update(item);
        return Response.noContent().build();
    }
}
