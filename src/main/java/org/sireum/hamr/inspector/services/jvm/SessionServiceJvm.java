package org.sireum.hamr.inspector.services.jvm;

import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.services.Session;
import org.sireum.hamr.inspector.services.SessionService;
import org.sireum.hamr.inspector.services.SessionStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import static org.sireum.hamr.inspector.services.jvm.ServiceUtils.JVM_SESSION;
import static org.sireum.hamr.inspector.services.jvm.ServiceUtils.SESSIONS;

@Component
public class SessionServiceJvm implements SessionService {

    private static final MonoProcessor<Long> startProcessor = MonoProcessor.create();
    private static final MonoProcessor<Long> stopProcessor = MonoProcessor.create();

    public static void setStart(long startTime) {
        startProcessor.onNext(startTime);
    }

    public static void setStop(long stopTime) {
        stopProcessor.onNext(stopTime);
    }

    @Override
    public @NotNull Flux<Session> sessions() {
        return SESSIONS.flux();
    }

    @Override
    public @NotNull Mono<Long> startTimeOf(@NotNull Session session) {
        if (!session.equals(JVM_SESSION)) {
            return Mono.empty();
        }

        return startProcessor;
    }

    @Override
    public @NotNull Mono<Long> stopTimeOf(@NotNull Session session) {
        if (!session.equals(JVM_SESSION)) {
            return Mono.empty();
        }

        if (stopProcessor.isSuccess()) {
            return stopProcessor;
        } else {
            return Mono.empty();
        }
    }

    @Override
    public @NotNull Mono<SessionStatus> statusOf(@NotNull Session session) {
        if (!session.equals(JVM_SESSION)) {
            return Mono.empty();
        }

        if (stopProcessor.isSuccess()) {
            return Mono.just(SessionStatus.COMPLETED);
        } else {
            return Mono.just(SessionStatus.RUNNING);
        }
    }

    @Override
    public @NotNull Flux<GroupedFlux<Session, SessionStatus>> liveStatusUpdates() {
        if (stopProcessor.isSuccess()) {
            return Flux.just(SessionStatus.COMPLETED).groupBy(status -> JVM_SESSION);
        } else {
            return Flux.just(SessionStatus.RUNNING)
                    .concatWith(stopProcessor.thenReturn(SessionStatus.COMPLETED))
                    .groupBy(status -> JVM_SESSION);
        }
    }

}
