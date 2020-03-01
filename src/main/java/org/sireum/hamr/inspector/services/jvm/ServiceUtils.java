package org.sireum.hamr.inspector.services.jvm;

import org.sireum.hamr.inspector.services.Session;
import reactor.core.publisher.Mono;

class ServiceUtils {

    static final Session JVM_SESSION = new Session("jvm");
    static final Mono<Session> SESSIONS = Mono.just(JVM_SESSION);

}
