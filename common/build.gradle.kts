val ktor_version: String by project
val serialization_version: String by project

plugins {
  kotlin("jvm")
  id("io.ktor.plugin")
  kotlin("plugin.serialization")
}

dependencies {
  implementation("io.ktor:ktor-network-jvm:$ktor_version")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
}
