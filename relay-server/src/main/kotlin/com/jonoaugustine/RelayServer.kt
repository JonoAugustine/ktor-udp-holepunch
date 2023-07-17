package com.jonoaugustine

import com.jonoaugustine.common.*
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

var groups = mapOf<UInt, PlayerGroup>()
val selectorManager = SelectorManager(Dispatchers.IO)
val mutex = Mutex()

suspend fun main() {
  val serverSocket = aSocket(selectorManager)
    .udp()
    .bind(Address.relayServerAddress.inet)

  println("relay server listening to ${serverSocket.localAddress}")

  serverSocket.incoming.consumeEach { datagram ->
    println("incoming datagram from ${datagram.address}")
    val text = datagram.packet.textOrNull()
    when (val packet = text?.toPacketOrNull()) {
      CreatePacket  -> {
        val group = newGroup(datagram)
        group.replicate(GroupPacket(group), serverSocket)
      }

      is JoinPacket -> {
        val group = join(datagram, packet.code)
        group?.replicate(GroupPacket(group), serverSocket)
      }

      null          -> println("null packet $text")
      else          -> println("${packet::class.simpleName} is unhandled")
    }

  }
}

private suspend fun newGroup(datagram: Datagram): PlayerGroup =
  1234u //Random.nextUInt()
    .also { println("generating new group $it...") }
    .let { code -> PlayerGroup(code, setOf(Player(Address(datagram.address), true))) }
    .also { group -> mutex.withLock { groups += group.code to group } }
    .also { println("...new group ${it.code}") }

private suspend fun join(datagram: Datagram, code: UInt): PlayerGroup? =
  mutex.withLock { groups[code] }
    ?.also { println("joining group $code") }
    ?.run { copy(players = players + Player(Address(datagram.address))) }
    ?.also { group -> mutex.withLock { groups += code to group } }
