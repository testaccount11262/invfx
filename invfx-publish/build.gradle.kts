import java.util.Base64

plugins {
    `maven-publish`
    signing
    id("com.gradleup.nmcp") version "0.0.8"
}

projectPlugin.tasks.named("clipJar") {
    dependsOn(tasks.named("publishApiPublicationToServerRepository"))
    dependsOn(tasks.named("publishCorePublicationToServerRepository"))
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "server"
            url = rootProject.uri(".server/libraries")
        }
    }

    publications {
        fun MavenPublication.setup(target: Project) {
            artifactId = target.name
            from(target.components["java"])
            artifact(target.tasks["sourcesJar"])
            artifact(target.tasks["dokkaJar"])

            pom {
                name.set(target.name)
                description.set("Kotlin DSL for PaperMC Inventory GUI")
                url.set("https://github.com/testaccount11262/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("monun")
                        name.set("Monun")
                        email.set("monun1010@gmail.com")
                        url.set("https://github.com/monun")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                    developer {
                        id.set("testaccount11262")
                        name.set("MariGold")
                        email.set("sanabi.real@gmail.com")
                        url.set("https://github.com/testaccount11262")
                        roles.addAll("Maintainer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/testaccount11262/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:testaccount11262/${rootProject.name}.git")
                    url.set("https://github.com/testaccount11262/${rootProject.name}")
                }
            }
        }

        create<MavenPublication>("api") {
            setup(projectApi)
        }

        create<MavenPublication>("core") {
            setup(projectCore)
            artifact(coreReobfJar)
        }
    }
}

signing {
    isRequired = true
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null) {
        val decodedKey = if (signingKey!!.trim().startsWith("-----BEGIN")) {
            signingKey!!.replace("\\n", "\n").replace("\r\n", "\n").trim()
        } else {
            String(java.util.Base64.getDecoder().decode(signingKey!!.trim()), Charsets.UTF_8)
        }

        if (signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, decodedKey, signingPassword)
        } else {
            useInMemoryPgpKeys(decodedKey, signingPassword)
        }
    }
    sign(publishing.publications["api"], publishing.publications["core"])
}


nmcp {
    publishAllPublications {
        username.set(providers.gradleProperty("mavenCentralUsername"))
        password.set(providers.gradleProperty("mavenCentralPassword"))
        publicationType.set("USER_MANAGED")
    }
}
