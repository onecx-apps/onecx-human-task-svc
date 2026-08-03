package org.tkit.onecx.human.task.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.human.task.domain.daos.TaskDAO;
import org.tkit.onecx.human.task.rs.external.v1.mappers.ExceptionMapper;
import org.tkit.onecx.human.task.rs.external.v1.mappers.TasksMapperV1;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.human.task.rs.external.v1.TasksV1Api;
import gen.org.tkit.onecx.human.task.rs.external.v1.model.CreateTaskRequestDTOV1;
import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProblemDetailResponseDTOV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class TasksRestControllerV1 implements TasksV1Api {

    @Inject
    TaskDAO dao;

    @Inject
    TasksMapperV1 mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createTask(CreateTaskRequestDTOV1 createTaskRequestDTOV1) {
        var task = mapper.toTask(createTaskRequestDTOV1);
        dao.create(task);
        return Response.noContent().build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
