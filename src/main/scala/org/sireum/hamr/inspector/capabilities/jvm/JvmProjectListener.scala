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

package org.sireum.hamr.inspector.capabilities.jvm

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

import art.Art.{PortId, Time}
import art.DataContent
import org.sireum.hamr.inspector.services.jvm.{MsgServiceJvm, SessionServiceJvm}

object JvmProjectListener {

  // each service will countDown by 1 after being initialized by spring
  // once all are initialized, the start() method is allowed to complete
  val serviceCountDownLatch = new CountDownLatch(3)

  private var startTime: Long = -1L
  private val atomicStartTime: AtomicLong = new AtomicLong(startTime)

  // scales an absolute timestamp to one that is relative to the start time
  // it is assumed calls to this method occur ONLY after startTime has been set,
  // but a check is performed to check for race conditions of the faster startTime
  // value vs the thread-safe (but slower) atomicStartTime
  private def deltaTime(time: Time): Long = {
    if (startTime != -1L) {
      // fast path
      time.toLong - startTime
    } else {
      // slow synchronized path
      time.toLong - atomicStartTime.get()
    }
  }

  def start(time: Time): Unit = {
    serviceCountDownLatch.await()

    startTime = time.toLong
    atomicStartTime.set(startTime)

    SessionServiceJvm.setStart(startTime)
  }

  def stop(time: Time): Unit = SessionServiceJvm.setStop(time.toLong)

  def output(src: PortId, dst: PortId, data: DataContent, time: Time): Unit = {
    MsgServiceJvm.commitNextMsg(src.toInt, dst.toInt, data, deltaTime(time))
  }

}
