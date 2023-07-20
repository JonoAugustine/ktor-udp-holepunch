package com.jonoaugustine

import com.jonoaugustine.common.*
import com.jonoaugustine.common.Address.Companion.RelayServerRemote
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() = coroutineScope {
  val selectorManager = SelectorManager(Dispatchers.IO)
  val socket = aSocket(selectorManager).udp()
    .bind(Address.local(50000).inet)
  var group: PlayerGroup? = null

  launch(Dispatchers.IO) {
    // Join Group
    socket.send(JoinPacket(1234u), RelayServerRemote)

    socket.incoming.consumeEach { datagram ->
      println("incoming datagram from ${datagram.address}")
      val text = datagram.packet.textOrNull()
      when (val packet = text?.toPacketOrNull()) {
        is GroupPacket -> {
          group = packet.group
          println(group!!.host)
          with(socket) { group!!.sendToHost(ChatMessage("connected")) }
        }

        is ChatMessage -> {
          println("chat message: ${packet.value}")
        }

        null           -> println("Null packet")
        else           -> println("${packet::class.simpleName} is unhandled")
      }
    }
  }

  while (true) {
    val line = readlnOrNull() ?: continue
    if (group == null) continue

    when (line.trim()) {
      "exit" -> break
      ""     -> println("ignored")
      else   -> with(socket) {
        println(socket.localAddress)
        group!!.sendToHost(ChatMessage(line.trim()))
      }
    }
  }
}
