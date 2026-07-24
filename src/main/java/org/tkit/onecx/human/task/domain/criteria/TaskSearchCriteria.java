package org.tkit.onecx.human.task.domain.criteria;

import java.util.List;

import org.tkit.onecx.human.task.domain.models.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskSearchCriteria {

    private String title;

    private String providerTaskId;

    private List<Task.Status> statuses;

    private String providerType;

    private Integer pageNumber;

    private Integer pageSize;
}
