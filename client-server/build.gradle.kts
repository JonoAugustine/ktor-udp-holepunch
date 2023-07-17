val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
  kotlin("jvm")
  id("io.ktor.plugin")
  kotlin("plugin.serialization")
}

application {
  mainClass.set("com.jonoaugustine.ClientKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
  implementation(project(":common"))
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
  implementation("io.ktor:ktor-network-jvm:$ktor_version")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

  implementation("ch.qos.logback:logback-classic:$logback_version")
}
