import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

val ENV = { key: String -> System.getenv(key) }

group = "io.github.null2264"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val testA by sourceSets.creating
val testB by sourceSets.creating

kotlinVersion("1.5.21", isPrimaryVersion = true)
kotlinVersion("1.6.20")
kotlinVersion("1.9.0")
kotlinVersion("2.0.0")

dependencies {
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.21")
    implementation(kotlin("stdlib"))
    api("org.cadixdev:lorenz:0.5.8")
    runtimeOnly("net.java.dev.jna:jna:5.10.0") // don't strictly need this but IDEA spams log without

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:4.6.3")

    testRuntimeOnly(testA.output)
    testRuntimeOnly(testB.output)
    testRuntimeOnly("org.spongepowered:mixin:0.8.4")
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("remap")
}

if (ENV("S3_ENDPOINT") != null) {
	System.setProperty("org.gradle.s3.endpoint", ENV("S3_ENDPOINT"))
}

publishing {
    publications {
        create("maven", MavenPublication::class) {
            from(components["java"])
        }
    }

    repositories {
        mavenLocal()
        if (ENV("AWS_ACCESS_KEY") != null && ENV("AWS_SECRET_KEY") != null) {
            maven {
                url = uri("s3://maven")
                credentials(AwsCredentials::class) {
                    accessKey = ENV("AWS_ACCESS_KEY")
                    secretKey = ENV("AWS_SECRET_KEY")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
    }
}

fun kotlinVersion(version: String, isPrimaryVersion: Boolean = false) {
    val name = version.replace(".", "")

    val sourceSet = sourceSets.create("kotlin$name")

    val testClasspath = configurations.create("kotlin${name}TestClasspath") {
        extendsFrom(configurations.testRuntimeClasspath.get())
        extendsFrom(configurations[sourceSet.compileOnlyConfigurationName])
    }

    dependencies {
        implementation(sourceSet.output)
        sourceSet.compileOnlyConfigurationName("org.jetbrains.kotlin:kotlin-compiler-embeddable:$version")
    }

    tasks.jar {
        from(sourceSet.output)
    }

    if (!isPrimaryVersion) {
        val testTask = tasks.register("testKotlin$name", Test::class) {
            useJUnitPlatform()
            testClassesDirs = sourceSets.test.get().output.classesDirs
            classpath = testClasspath + sourceSets.test.get().output + sourceSets.main.get().output
        }
        tasks.check { dependsOn(testTask) }
    }
}
