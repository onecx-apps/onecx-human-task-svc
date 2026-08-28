package org.tkit.onecx.human.task.rs.adapter.client;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigDocFilename("human-task-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "adapter")
public interface AdapterConfig {

    /**
     * Adapter urls per provider type (e.g. adapter.urls.CAMUNDA).
     */
    Map<String, String> urls();
}