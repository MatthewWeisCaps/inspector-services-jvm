package org.sireum.hamr.inspector.services.jvm;

import art.Bridge;
import art.DataContent;
import art.UPort;
import org.jetbrains.annotations.NotNull;
import org.sireum.hamr.inspector.capabilities.jvm.JvmProjectListener;
import org.sireum.hamr.inspector.common.ArtUtils;
import org.sireum.hamr.inspector.common.Msg;
import org.sireum.hamr.inspector.services.MsgService;
import org.sireum.hamr.inspector.services.Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.*;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MsgServiceJvm implements MsgService {

    private static AtomicLong msgCounter = new AtomicLong();

    private static final FluxProcessor<Msg, Msg> msgProcessor = ReplayProcessor.create();
    private static final FluxSink<Msg> msgSink = msgProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

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
    public @NotNull Flux<Msg> replayThenLive(@NotNull Session session) {
        return msgProcessor;
    }

    @Override
    public @NotNull Flux<Msg> replay(@NotNull Session session) {
        return msgProcessor.take(msgCounter.longValue());
    }

    @Override
    public @NotNull Flux<Msg> live(@NotNull Session session) {
        return msgProcessor.skip(msgCounter.longValue());
    }

    @Override
    public @NotNull Mono<Long> count(@NotNull Session session) {
        return Mono.just(msgCounter.longValue());
    }

}
