import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit5Version = "5.6.1"
val junitPlatformVersion = "1.6.1"

buildscript {
    repositories {
        mavenCentral()
    }
    val pitest by configurations.creating
    dependencies {
//        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.7")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.70")
        pitest("org.pitest:pitest-junit5-plugin:0.12")
    }
}

group = "pitest.test.kotlin"

apply(plugin = "java")
apply(plugin ="org.jetbrains.kotlin.jvm")
apply(plugin = "info.solidsoft.pitest")

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    "testImplementation"("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
    testPlugin.set("junit5")
    avoidCallsTo.set(setOf("kotlin.jvm.internal"))
    mutators.set(setOf("STRONGER"))
    targetClasses.set(setOf("pitest.test.*"))  //by default "${project.group}.*"
    targetTests.set(setOf("pitest.test.*Test"))
//    pitestVersion.set("1.4.0")   //current defined for Gradle plugin PIT version should be used
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(setOf("XML", "HTML"))
}
