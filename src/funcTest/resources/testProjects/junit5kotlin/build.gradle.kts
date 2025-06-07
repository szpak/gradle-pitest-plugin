import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit5Version = "5.7.0"
val junitPlatformVersion = "1.7.0"

//"plugins {}" cannot be simply used as there is problem with resolving build plugins with classpath modifications made by nebula-test
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
//        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.7")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}

group = "pitest.test.kotlin"

apply(plugin = "java")
apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "info.solidsoft.pitest")

//For some reason "java.sourceCompatibility = ..." no longer works
configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

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
    junit5PluginVersion.set("1.0.0")
    avoidCallsTo.set(setOf("kotlin.jvm.internal"))
    mutators.set(setOf("STRONGER"))
    targetClasses.set(setOf("pitest.test.*"))  //by default "${project.group}.*"
    targetTests.set(setOf("pitest.test.*Test"))
//    pitestVersion.set("1.4.0")   //current defined for Gradle plugin PIT version should be used
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(setOf("XML", "HTML"))
}
