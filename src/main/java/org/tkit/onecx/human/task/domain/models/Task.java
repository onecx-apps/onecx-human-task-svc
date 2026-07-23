package org.tkit.onecx.human.task.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TASK")
@SuppressWarnings("squid:S2160")
public class Task extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    @Column(name = "PROVIDER_TYPE", nullable = false)
    private String providerType;

    @Column(name = "PROVIDER_TASK_ID", nullable = false)
    private String providerTaskId;

    @Column(name = "PROVIDER_URL", nullable = false)
    private String providerURL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "CUSTOM_INPUT", columnDefinition = "jsonb")
    private java.util.Map<String, String> customInput;

    public enum Status {
        CREATED,
        ACCEPTED,
        DECLINED,
        ABORTED
    }
}
