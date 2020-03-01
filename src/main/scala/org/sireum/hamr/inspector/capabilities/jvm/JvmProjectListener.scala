package org.sireum.hamr.inspector.capabilities.jvm

import java.util.concurrent.CountDownLatch

import art.Art.{PortId, Time}
import art.DataContent
import org.sireum.hamr.inspector.services.jvm.{MsgServiceJvm, SessionServiceJvm}

object JvmProjectListener {

  // each service will countDown by 1 after being initialized by spring
  // once all are initialized, the start() method is allowed to complete
  val serviceCountDownLatch = new CountDownLatch(3)

  def start(time: Time): Unit = {
    serviceCountDownLatch.await()
    SessionServiceJvm.setStart(time.toLong)
  }

  def stop(time: Time): Unit = SessionServiceJvm.setStop(time.toLong)

  def output(src: PortId, dst: PortId, data: DataContent, time: Time): Unit = {
    MsgServiceJvm.commitNextMsg(src.toInt, dst.toInt, data, time.toLong)
  }

}
