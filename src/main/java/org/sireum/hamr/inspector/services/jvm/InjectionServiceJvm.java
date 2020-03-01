package org.sireum.hamr.inspector.services.jvm;

import art.ArtDebug;
import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.common.Injection;
import org.sireum.hamr.inspector.common.InspectionBlueprint;
import org.sireum.hamr.inspector.services.InjectionService;
import org.sireum.hamr.inspector.services.Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

@Component
public class InjectionServiceJvm implements InjectionService {

    public static FluxSink<String> hi;

    private final InspectionBlueprint inspectionBlueprint;

    public InjectionServiceJvm(InspectionBlueprint inspectionBlueprint) {
        this.inspectionBlueprint = inspectionBlueprint;
    }

    @Override
    public void inject(@NotNull Session session, @NotNull Injection injection) {
        if (session.equals(ServiceUtils.JVM_SESSION)) {
            ArtDebug.injectPort(injection.bridge().id(), injection.port().id(), injection.dataContent());
        }
    }

}
