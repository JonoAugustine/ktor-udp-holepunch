package com.jonoaugustine

import com.jonoaugustine.common.*
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString

suspend fun main() = runBlocking {
  val selectorManager = SelectorManager(Dispatchers.IO)
  val clientHostSocket = aSocket(selectorManager).udp().bind()

  createGroup(clientHostSocket, Address.relayServerAddress)
  var group: PlayerGroup? = null

  clientHostSocket.incoming.consumeEach { datagram ->
    println("incoming datagram from ${datagram.address}")
    val text = datagram.packet.textOrNull()
    when (val packet = text?.toPacketOrNull()) {
      is ChatMessage -> {
        println(packet.value)
        group?.replicate(packet, clientHostSocket)
      }

      is GroupPacket -> {
        group = packet.group
        println(group)
      }

      null           -> println("Null packet type $text")
      else           -> println("${packet::class.simpleName} should never be sent to this client")
    }
  }
}

/**
 * Sends the [CreatePacket] request to the given [holePunchSocket]
 */
suspend fun createGroup(holePunchSocket: BoundDatagramSocket, address: Address) =
  JsonConfig.encodeToString<RequestPacket>(CreatePacket)
    .run { ByteReadPacket(toByteArray()) }
    .let { Datagram(it, address.inet) }
    .let { holePunchSocket.send(it) }
