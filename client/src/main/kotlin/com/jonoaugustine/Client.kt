package com.jonoaugustine

import com.jonoaugustine.common.*
import com.jonoaugustine.common.Address.Companion.relayServerAddress
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.toJavaAddress
import io.ktor.util.network.port
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun main() = runBlocking {
  val selectorManager = SelectorManager(Dispatchers.IO)
  val udpSocket = aSocket(selectorManager).udp().bind()
  var group: PlayerGroup? = null

  launch(Dispatchers.IO) {
    // Join Group
    udpSocket.send(JoinPacket(1234u), relayServerAddress)

    udpSocket.incoming.consumeEach { datagram ->
      println("incoming datagram from ${datagram.address}")
      val text = datagram.packet.textOrNull()
      when (val packet = text?.toPacketOrNull()) {
        is GroupPacket -> {
          group = packet.group
          println(group)
        }

        is ChatMessage -> println(packet.value)
        null           -> println("Null packet")
        else           -> println("${packet::class.simpleName} is unhandled")
      }
    }
  }

  var reading = true
  while (reading) readln()
    .takeIf { group != null }
    ?.let {
      when {
        it == "exit"    -> reading = false
        it.isNotBlank() -> udpSocket.send(ChatMessage(it), group!!.host.address)
      }
    }
}
