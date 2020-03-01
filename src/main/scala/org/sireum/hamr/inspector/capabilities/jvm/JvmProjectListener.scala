package org.sireum.hamr.inspector.capabilities.jvm

import art.Art.{PortId, Time}
import art.DataContent
import org.sireum.hamr.inspector.services.jvm.{MsgServiceJvm, SessionServiceJvm}

object JvmProjectListener {

  def start(time: Time): Unit = {
    SessionServiceJvm.setStart(time.toLong)
  }

  def stop(time: Time): Unit = SessionServiceJvm.setStop(time.toLong)

  def output(src: PortId, dst: PortId, data: DataContent, time: Time): Unit = {
    MsgServiceJvm.commitNextMsg(src.toInt, dst.toInt, data, time.toLong)
  }

}
