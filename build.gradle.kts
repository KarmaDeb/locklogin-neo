plugins {
    id("java")
}

group = "com.github.karmadeb"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

subprojects {
    apply {
        plugin("java")
    }

    if (project.name != "neo-api") {
        dependencies {
            implementation(project(":neo-api"))
        }
    }

    if (project.name != "neo-api" && project.name != "neo-api-bridge") {
        dependencies {
            implementation(project(":neo-api-bridge"))
        }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2")
    }
}