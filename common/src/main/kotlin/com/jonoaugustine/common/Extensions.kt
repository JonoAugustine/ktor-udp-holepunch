package com.jonoaugustine.common

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.serialization.encodeToString

fun SelectorManager.connectTo(host: String, port: Int): ConnectedDatagramSocket =
  aSocket(this).udp().connect(InetSocketAddress(host, port))

fun ByteReadPacket.textOrNull(): String? = takeIf { canRead() }?.readText()

suspend inline fun <reified T : Packet> BoundDatagramSocket.send(
  packet: T,
  address: Address
) = JsonConfig.encodeToString<Packet>(packet)
  .toByteArray()
  .let { ByteReadPacket(it) }
  .let { send(Datagram(it, address.inet)) }

/** Sends the given [packet] to all player clients */
context(BoundDatagramSocket)
suspend inline fun <reified T : Packet> PlayerGroup.sendToAll(packet: T) =
  players.forEach { send(packet, it.address) }

/** Sends the given [packet] to all player clients excluding the host */
context(BoundDatagramSocket)
suspend inline fun <reified T : Packet> PlayerGroup.sendToClients(packet: T) =
  players.filterNot { it.host }.forEach { send(packet, it.address) }

/** Sends the given [packet] to the group host */
context(BoundDatagramSocket)
suspend inline fun <reified T : Packet> PlayerGroup.sendToHost(packet: T) =
  send(packet, players.first { it.host }.address)

