package org.tkit.onecx.human.task.rs.adapter.client;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import gen.org.tkit.onecx.human.task.rs.adapter.model.ProcessTaskRequestAdapterDTO;

@Path("/v1/tasks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TasksAdapterClient {

    @POST
    @Path("/accept")
    Response acceptTask(@NotNull ProcessTaskRequestAdapterDTO request);

    @POST
    @Path("/decline")
    Response declineTask(@NotNull ProcessTaskRequestAdapterDTO request);
}
