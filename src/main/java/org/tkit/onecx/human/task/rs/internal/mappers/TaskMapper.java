package org.tkit.onecx.human.task.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.human.task.domain.criteria.TaskSearchCriteria;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.human.task.rs.internal.model.TaskDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskPageResultDTO;
import gen.org.tkit.onecx.human.task.rs.internal.model.TaskSearchCriteriaDTO;

@Mapper(uses = { OffsetDateTimeMapper.class, ProviderTypeMapper.class })
public interface TaskMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id")
    @Mapping(target = "modificationCount")
    @Mapping(target = "creationDate")
    @Mapping(target = "creationUser")
    @Mapping(target = "modificationDate")
    @Mapping(target = "modificationUser")
    @Mapping(target = "title")
    @Mapping(target = "description")
    @Mapping(target = "status")
    @Mapping(target = "providerType")
    @Mapping(target = "providerTaskId")
    @Mapping(target = "providerURL")
    @Mapping(target = "customInput")
    @Mapping(target = "removeCustomInputItem", ignore = true)
    TaskDTO toTaskDTO(Task task);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "statuses")
    @Mapping(target = "providerType")
    @Mapping(target = "pageNumber")
    @Mapping(target = "pageSize")
    TaskSearchCriteria toTaskSearchCriteria(TaskSearchCriteriaDTO dto);

    @Mapping(target = "number", expression = "java((int) page.getNumber())")
    @Mapping(target = "size", expression = "java((int) page.getSize())")
    @Mapping(target = "stream", source = "stream")
    @Mapping(target = "removeStreamItem", ignore = true)
    TaskPageResultDTO map(PageResult<Task> page);

    List<TaskDTO> map(Stream<Task> stream);

}
