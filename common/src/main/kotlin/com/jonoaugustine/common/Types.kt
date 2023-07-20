package com.jonoaugustine.common

import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.toJavaAddress
import io.ktor.util.network.address
import io.ktor.util.network.port
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.net.SocketAddress
import io.ktor.network.sockets.SocketAddress as KtorSocketAddress

val JsonConfig = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
  encodeDefaults = true
  serializersModule = SerializersModule {
    polymorphic(Packet::class) {
      subclass(JoinPacket::class)
      subclass(CreatePacket::class)
      subclass(GroupPacket::class)
      subclass(ChatMessage::class)
    }
    polymorphic(RequestPacket::class) {
      subclass(JoinPacket::class)
      subclass(CreatePacket::class)
    }
    polymorphic(ResponsePacket::class) {
      subclass(GroupPacket::class)
    }
  }
}

@Serializable
data class Address(val ip: String, val port: Int) {

  constructor(address: SocketAddress) : this(address.address, address.port)
  constructor(address: KtorSocketAddress) : this(address.toJavaAddress())

  companion object {

    val RelayServerLocal = Address("0.0.0.0", 7001)
    val RelayServerRemote = Address("127.0.0.1", 7001)
    fun local(port: Int): Address = Address("localhost", port)
  }
}

val Address.inet get() = InetSocketAddress(ip, port)

@Serializable
data class Player(val address: Address, val host: Boolean = false) {

  override fun equals(other: Any?): Boolean = address == (other as? Player)?.address
  override fun hashCode(): Int = address.hashCode()
}

@Serializable
data class PlayerGroup(
  val code: UInt,
  val players: Set<Player> = setOf(),
  val limit: Byte = 2
) {

  override fun equals(other: Any?) = other is PlayerGroup && other.code == code
  override fun hashCode() = code.hashCode()
}

val PlayerGroup.ready: Boolean get() = players.size == limit.toInt()

val PlayerGroup.host: Player get() = players.first { it.host }

@Serializable
sealed interface Packet

@Serializable
sealed interface RequestPacket : Packet

@Serializable
@SerialName("chat.message")
data class ChatMessage(val value: String) : Packet

@Serializable
@SerialName("join")
data class JoinPacket(val code: UInt) : RequestPacket

@Serializable
@SerialName("create")
data object CreatePacket : RequestPacket

@Serializable
sealed interface ResponsePacket : Packet

@Serializable
@SerialName("group")
data class GroupPacket(val group: PlayerGroup) : ResponsePacket

fun String.toPacketOrNull(): Packet? =
  runCatching { JsonConfig.decodeFromString<Packet>(this) }.getOrNull()
