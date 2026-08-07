package org.tkit.onecx.human.task.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

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

    @Mapping(target = "removeCustomInputItem", ignore = true)
    TaskDTO toTaskDTO(Task task);

    TaskSearchCriteria toTaskSearchCriteria(TaskSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    TaskPageResultDTO totaskPageResultDTO(PageResult<Task> page);

    List<TaskDTO> map(Stream<Task> stream);

}
