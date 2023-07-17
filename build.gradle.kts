plugins {
  kotlin("jvm") version "1.9.0" apply false
  id("io.ktor.plugin") version "2.3.2" apply false
  kotlin("plugin.serialization") version "1.9.0" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.jonoaugustine"
  version = "0.0.1"
}
