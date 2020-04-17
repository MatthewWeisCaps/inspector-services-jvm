/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.hamr.inspector.services.jvm;

import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.capabilities.jvm.JvmProjectListener;
import org.sireum.hamr.inspector.services.Session;
import org.sireum.hamr.inspector.services.SessionService;
import org.sireum.hamr.inspector.services.SessionStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import javax.annotation.PostConstruct;

import static org.sireum.hamr.inspector.services.jvm.ServiceUtils.JVM_SESSION;
import static org.sireum.hamr.inspector.services.jvm.ServiceUtils.SESSIONS;

@Component
public class SessionServiceJvm implements SessionService {

    static final MonoProcessor<Long> startProcessor = MonoProcessor.create();
    static final MonoProcessor<Long> stopProcessor = MonoProcessor.create();

    @PostConstruct
    private void postConstruct() {
        JvmProjectListener.serviceCountDownLatch().countDown();
    }

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
