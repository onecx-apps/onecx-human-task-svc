package org.tkit.onecx.human.task.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.onecx.human.task.rs.mappers.ProviderTypeMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.human.task.rs.external.v1.model.CreateTaskRequestDTOV1;

@Mapper(uses = { OffsetDateTimeMapper.class, ProviderTypeMapper.class })
public interface TasksMapperV1 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "customInput", ignore = true)
    Task toTask(CreateTaskRequestDTOV1 dto);

}
