plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("com.gradle.plugin-publish") version "1.1.0"
    signing
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    api("com.github.kotlinx:ast:0.1.0")

    implementation("xyz.ronella.gradle.plugin:simple-git:2.0.1")
    implementation("space.kscience:plotlykt-core:0.5.0")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.1")
}

kotlin {
    jvmToolchain(11)
}

group = "io.github.alecarnevale"
version = "1.0.0"

gradlePlugin {
    plugins {
        website.set("https://github.com/alecarnevale/abacus-plugin")
        vcsUrl.set("https://github.com/alecarnevale/abacus-plugin.git")
        create("abacusPlugin") {
            id = "io.github.alecarnevale.abacus"
            displayName = "Abacus Plugin"
            description = "Counts and plots the number of files by extension or by inheritance."
            implementationClass = "com.alecarnevale.abacus.AbacusPlugin"
            tags.set(listOf("java", "kotlin", "analysis"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../../local-plugin-repository")
        }
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}