package org.tkit.onecx.human.task.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TASK")
@Getter
@Setter
public class Task extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STATUS")
    private Status status;

    @Column(name = "PROVIDER_TYPE")
    private ProviderType providerType;

    @Column(name = "PROVIDER_TASK_ID")
    private String providerTaskId;
    
    @Column(name = "PROVIDER_URL")
    private String providerURL;

    public enum Status {
        CREATED,
        ACCEPTED,
        DECLINED,
        ABORTED
    }

    public enum ProviderType {
        CAMUNDA,
        N8N
    }
}
