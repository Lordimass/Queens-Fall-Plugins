import org.gradle.internal.snapshot.impl.ArrayOfPrimitiveValueSnapshot

plugins {
    idea
    java
    alias(libs.plugins.hytaleTools)
    alias(libs.plugins.hytalePublisher)
    id("com.gradleup.shadow") version "9.4.2"
}

// Plugin versions are sourced from gradle/libs.versions.toml.


tasks.withType<Javadoc>().configureEach {
    (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

group = project.property("group").toString()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(property("java_version").toString().toInt()))
}

val lombokVersion = "1.18.40"
dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.jspecify:jspecify:1.0.0")

    // CurseMaven Hytale Libraries
    implementation("curse.maven:hyui-1431415:8151837")
    implementation("curse.maven:creditor-1560961:8334074")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

hytaleTools {
    javaVersion = property("java_version").toString().toInt()
    hytaleVersion = property("hytale_version").toString()
    manifestServerVersion = property("manifestServerVersion").toString()
    manifestGroup = property("manifest_group").toString()
    modId = property("mod_id").toString()
    modDescription = property("mod_description").toString()
    modUrl = property("mod_url").toString()
    mainClass = property("main_class").toString()
    modCredits = property("mod_author").toString()
    manifestDependencies = property("manifest_dependencies").toString()
    manifestOptionalDependencies = property("manifest_opt_dependencies").toString()
    curseforgeId = property("curseforgeID").toString()
    disabledByDefault = property("disabled_by_default").toString().toBoolean()
    includesPack = property("includes_pack").toString().toBoolean()
    patchline = property("patchline").toString()
}

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

hytalePublisher {
    version = property("version").toString()
    gameVersion = property("hytale_version").toString()
    releaseType = "release"
    changelogFile = "changelog.md"

    curseforge {
        enabled = true
        projectId = property("curseforge_project_id").toString()

        embeddedLibrary("hyui")
        required("creditor")
    }

    modifold {
        enabled = true
        projectId = property("modifold_project_id").toString()
        gameVersions = listOf(property("hytale_version").toString())
        loaders = listOf("Vanilla")

        optional("Cpvw9Q")
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        // Clean up manifest
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Multi-Release" to "true"
            )
        }

        // Include asset pack at JAR root level.
        // Hytale's asset loader expects Server/ and Common/ at the JAR root
        // when IncludesAssetPack is true in manifest.json.
        // Without this, assets end up nested under hytale-assets/ which Hytale ignores.
        // Exclude the asset pack's own manifest.json — our plugin manifest at root already
        // declares IncludesAssetPack:true, and the asset pack manifest would overwrite it
        // (replacing the Main class entry, causing a classloader NPE on startup).
        from("src/main/resources/hytale-assets") {
            exclude("manifest.json")
        }

        // Remove the nested hytale-assets/ copy from the JAR (assets are at root now).
        // The nested copy still exists in build/resources/main/ for deploy.sh compatibility.
        exclude("hytale-assets/**")

        // Exclude signature files (cause issues)
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")

        // NOTE: Relocation disabled - Shadow plugin's ASM doesn't support Java 25 bytecode
        // SnakeYAML is bundled without relocation. If conflicts occur with other plugins,
        // consider using a different config library or waiting for Shadow plugin update.
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false // Only use shadowJar
    }
}