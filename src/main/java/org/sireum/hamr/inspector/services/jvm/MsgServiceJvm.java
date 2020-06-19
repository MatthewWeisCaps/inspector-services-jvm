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

import art.Bridge;
import art.DataContent;
import art.UPort;
import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.capabilities.jvm.JvmProjectListener;
import org.sireum.hamr.inspector.common.ArtUtils;
import org.sireum.hamr.inspector.common.Msg;
import org.sireum.hamr.inspector.services.MsgService;
import org.sireum.hamr.inspector.services.RecordId;
import org.sireum.hamr.inspector.services.Session;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;
import reactor.core.publisher.*;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MsgServiceJvm implements MsgService {

    private static AtomicLong msgCounter = new AtomicLong();

    private static final Flux<Msg> msgFlux;
    private static final FluxSink<Msg> msgSink;

    static {
        final FluxProcessor<Msg, Msg> msgProcessor = ReplayProcessor.create();
        msgSink = msgProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        msgFlux = msgProcessor.takeUntilOther(SessionServiceJvm.stopProcessor.then());
    }

    private static volatile ArtUtils artUtils;

    @PostConstruct
    private void postConstruct() {
        JvmProjectListener.serviceCountDownLatch().countDown();
    }

    public MsgServiceJvm(ArtUtils artUtils) {
        MsgServiceJvm.artUtils = artUtils;
    }

    public synchronized static void commitNextMsg(int srcPortId, int dstPortId, @NotNull DataContent data, long time) {
        final long uid = msgCounter.getAndIncrement();
        final UPort src = artUtils.getPort(srcPortId);
        final UPort dst = artUtils.getPort(dstPortId);
        final Bridge srcBridge = artUtils.getBridge(src);
        final Bridge dstBridge = artUtils.getBridge(dst);
        msgSink.next(new Msg(src, dst, srcBridge, dstBridge, data, time, uid));
    }

    @Override
    public @NotNull Mono<Long> count(@NotNull Session session) {
        return Mono.just(msgCounter.longValue());
    }

    // todo mock range on jvm - currently just returns all for jvm impl

    @Override
    public @NotNull Flux<Msg> live(@NotNull Session session, @NotNull Range<RecordId> range) {
        return msgFlux;
    }

    @Override
    public @NotNull Flux<Msg> replay(@NotNull Session session, @NotNull Range<RecordId> range) {
        return msgFlux.take(msgCounter.longValue());
    }

    @Override
    public @NotNull Flux<Msg> replayReverse(@NotNull Session session, @NotNull Range<RecordId> range) {
         throw new UnsupportedOperationException("todo for jvm");
    }

}
