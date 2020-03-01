package org.sireum.hamr.inspector.services.jvm;

import art.ArtDebug;
import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.capabilities.jvm.JvmProjectListener;
import org.sireum.hamr.inspector.common.Injection;
import org.sireum.hamr.inspector.services.InjectionService;
import org.sireum.hamr.inspector.services.Session;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class InjectionServiceJvm implements InjectionService {

    @PostConstruct
    private void postConstruct() {
        JvmProjectListener.serviceCountDownLatch().countDown();
    }

    @Override
    public void inject(@NotNull Session session, @NotNull Injection injection) {
        if (session.equals(ServiceUtils.JVM_SESSION)) {
            ArtDebug.injectPort(injection.bridge().id(), injection.port().id(), injection.dataContent());
        }
    }

}
