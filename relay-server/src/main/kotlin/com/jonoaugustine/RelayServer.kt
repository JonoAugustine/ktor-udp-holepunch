package com.jonoaugustine

import com.jonoaugustine.common.*
import io.ktor.network.selector.SelectorManager
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
    .bind(Address.RelayServerLocal.inet)

  println("relay server listening to ${serverSocket.localAddress}")

  serverSocket.incoming.consumeEach { datagram ->
    println("incoming datagram from ${datagram.address}")
    val text = datagram.packet.textOrNull()
    with(serverSocket) {
      when (val packet = text?.toPacketOrNull()) {
        CreatePacket  -> {
          val group = newGroup(Address(datagram.address))
          group.sendToAll(GroupPacket(group))
        }

        is JoinPacket -> {
          val group = join(Address(datagram.address), packet.code)
          group?.sendToAll(GroupPacket(group))
        }

        null          -> println("null packet $text")
        else          -> println("${packet::class.simpleName} is unhandled")
      }
    }
  }
}

private suspend fun newGroup(address: Address): PlayerGroup =
  1234u //Random.nextUInt()
    .also { println("\tgenerating new group $it") }
    .let { code -> PlayerGroup(code, setOf(Player(address, true))) }
    .also { group -> mutex.withLock { groups += group.code to group } }

private suspend fun join(address: Address, code: UInt): PlayerGroup? =
  mutex.withLock { groups[code] }
    ?.also { println("\tjoining group $code") }
    ?.run { copy(players = players + Player(address)) }
    ?.also { group -> mutex.withLock { groups += code to group } }
