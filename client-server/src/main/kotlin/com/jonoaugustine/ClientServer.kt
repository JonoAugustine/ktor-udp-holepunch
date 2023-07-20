package com.jonoaugustine

import com.jonoaugustine.common.*
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val selectorManager = SelectorManager(Dispatchers.IO)
val hostSocket = aSocket(selectorManager).udp()
  .bind(Address.local(6000).inet)
lateinit var group: PlayerGroup

suspend fun main() = coroutineScope {
  /** Sends the [CreatePacket] request to the relay server */
  /** Sends the [CreatePacket] request to the relay server */
  hostSocket.send(CreatePacket, Address.RelayServerRemote)

  launch(Dispatchers.IO) {
    hostSocket.incoming.consumeEach { datagram ->
      println("incoming datagram from ${datagram.address}")
      val text = datagram.packet.textOrNull()
      when (val packet = text?.toPacketOrNull()) {
        is GroupPacket -> {
          group = packet.group
          println(group)
          with(hostSocket) { group.sendToClients(packet) }
        }

        is ChatMessage -> {
          println("chat message: ${packet.value}")
          with(hostSocket) { group.sendToClients(packet) }
        }

        null           -> println("Null packet type $text")
        else           -> println("${packet::class.simpleName} is unhandled")
      }
    }
  }

  while (true) {
    val line = readlnOrNull() ?: continue

    when (line.trim()) {
      "exit" -> break
      ""     -> println("ignored")
      else   -> with(hostSocket) { group.sendToHost(ChatMessage(line.trim())) }
    }
  }
}
