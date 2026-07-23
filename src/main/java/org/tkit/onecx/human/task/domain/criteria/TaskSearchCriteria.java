package org.tkit.onecx.human.task.domain.criteria;

import org.tkit.onecx.human.task.domain.models.Task;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class TaskSearchCriteria {

    private String title;

    private String providerTaskId;

    private Task.Status status;

    private String providerType;

    private Integer pageNumber;

    private Integer pageSize;
}
