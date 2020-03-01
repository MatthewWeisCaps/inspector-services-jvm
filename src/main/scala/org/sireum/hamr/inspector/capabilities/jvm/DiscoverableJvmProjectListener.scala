package org.sireum.hamr.inspector.capabilities.jvm

import art.Art.{PortId, Time}
import art.DataContent
import org.sireum.hamr.inspector.capabilities.ProjectListener

class DiscoverableJvmProjectListener extends ProjectListener {

  override def start(time: Time): Unit = JvmProjectListener.start(time)

  override def stop(time: Time): Unit = JvmProjectListener.stop(time)

  override def output(src: PortId, dst: PortId, data: DataContent, time: Time): Unit = JvmProjectListener.output(src, dst, data, time)

}
