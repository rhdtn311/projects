rootProject.name = "projects"

// Use composite build since the subdirectory has its own settings.gradle.kts
// This avoids conflicts and lets the existing wrapper/configs in pg-routing stay intact
includeBuild("pg-routing")

// Optional: Centralize plugin repositories so subprojects can resolve plugins consistently
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/release")
    }
}

// Optional: Centralize dependency repositories for all included builds
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.spring.io/release")
    }
}
