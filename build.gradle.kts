plugins {
    id("application")
    id("io.freefair.lombok") version "9.5.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.outlook.wn123o"
version = "1.0-SNAPSHOT"

application {
    mainClass = "Main"
}

repositories {
    mavenCentral()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

dependencies {
    implementation("de.vandermeer:asciitable:0.3.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.23.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}