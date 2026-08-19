package org.tkit.onecx.human.task.rs.internal.controllers;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.human.task.domain.daos.TaskDAO;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.onecx.human.task.rs.adapter.client.AdapterClientService;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.rs.internal.mappers.TaskMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.human.task.rs.adapter.model.ProcessTaskRequestAdapterDTO;
import gen.org.tkit.onecx.human.task.rs.adapter.model.ProviderTypeAdapterDTO;
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

    @Inject
    AdapterClientService adapterClientService;

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
        var item = prepareTaskUpdate(id, acceptTaskRequestDTO.getModificationCount(), acceptTaskRequestDTO.getInput(),
                Task.Status.ACCEPTED);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }
        var adapterResponse = callAdapter(item, acceptTaskRequestDTO.getInput(), true);
        if (adapterResponse.getStatus() == 204) {
            persistTask(item);
        }
        return adapterResponse;
    }

    @Override
    public Response declineTask(String id, DeclineTaskRequestDTO declineTaskRequestDTO) {
        var item = prepareTaskUpdate(id, declineTaskRequestDTO.getModificationCount(), declineTaskRequestDTO.getInput(),
                Task.Status.DECLINED);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }
        var adapterResponse = callAdapter(item, declineTaskRequestDTO.getInput(), false);
        if (adapterResponse.getStatus() == 204) {
            persistTask(item);
        }
        return adapterResponse;
    }

    @Override
    public Response searchTasksByCriteria(TaskSearchCriteriaDTO taskSearchCriteriaDTO) {
        var result = dao.findTasksByCriteria(mapper.toTaskSearchCriteria(taskSearchCriteriaDTO));
        return Response.ok(mapper.totaskPageResultDTO(result)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> optimisticLockException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }

    private Task prepareTaskUpdate(String id, Integer modificationCount, Map<String, String> input, Task.Status status) {
        var item = dao.findById(id);
        if (item == null || item.getStatus() != Task.Status.CREATED) {
            return null;
        }
        item.setModificationCount(modificationCount);
        item.setStatus(status);
        item.setCustomInput(input);
        return item;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    void persistTask(Task item) {
        dao.update(item);
    }

    private Response callAdapter(Task item, Map<String, String> input, boolean accept) {
        var client = adapterClientService.getClient(item.getProviderType());
        if (client == null) {
            var dto = exceptionMapper.exception(ExceptionMapper.ErrorKeys.INVALID_PROVIDER.name(),
                    "No adapter configured for provider: " + item.getProviderType());
            return Response.status(400).entity(dto).build();
        }
        var request = new ProcessTaskRequestAdapterDTO()
                .providerType(ProviderTypeAdapterDTO.fromValue(item.getProviderType()))
                .providerTaskId(item.getProviderTaskId())
                .providerURL(item.getProviderURL())
                .customInput(input);
        try (Response response = accept ? client.acceptTask(request) : client.declineTask(request)) {
            return Response.status(response.getStatus()).build();
        }
    }
}
