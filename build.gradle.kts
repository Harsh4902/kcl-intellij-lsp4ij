import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.24"
  id("org.jetbrains.intellij") version "1.17.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html

intellij {
  version.set("2023.2.6")
  type.set("IC") // Target IDE Platform

  val platformPlugins =  ArrayList<Any>()
  val latestLsp4ijNightlyVersion = fetchLatestLsp4ijNightlyVersion()
  platformPlugins.add("com.redhat.devtools.lsp4ij:$latestLsp4ijNightlyVersion@nightly")
  plugins = platformPlugins;
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

fun fetchLatestLsp4ijNightlyVersion(): String {
  val client = HttpClient.newBuilder().build();
  var onlineVersion = ""
  try {
    val request: HttpRequest = HttpRequest.newBuilder()
      .uri(URI("https://plugins.jetbrains.com/api/plugins/23257/updates?channel=nightly&size=1"))
      .GET()
      .timeout(Duration.of(10, ChronoUnit.SECONDS))
      .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    val pattern = Pattern.compile("\"version\":\"([^\"]+)\"")
    val matcher = pattern.matcher(response.body())
    if (matcher.find()) {
      onlineVersion = matcher.group(1)
      println("Latest approved nightly build: $onlineVersion")
    }
  } catch (e:Exception) {
    println("Failed to fetch LSP4IJ nightly build version: ${e.message}")
  }

  val minVersion = "0.0.1-20231213-012910"
  return if (minVersion < onlineVersion) onlineVersion else minVersion
}