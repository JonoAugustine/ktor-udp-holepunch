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

suspend inline fun <reified T : Packet> PlayerGroup.replicate(
  packet: T,
  socket: BoundDatagramSocket
) = players
  .filterNot { it.host }
  .forEach { socket.send(packet, it.address) }
