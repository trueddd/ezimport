plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.1.3"
    id("org.jetbrains.compose") version "0.4.0"
}

group = "com.github.trueddd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.jetbrains.compose.material:material:")
    implementation(compose.desktop.currentOs)
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("211.7628.21")
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    patchPluginXml {
        changeNotes.set("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
    }
}
