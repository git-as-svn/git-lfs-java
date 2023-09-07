import org.ajoberstar.grgit.Grgit
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
val signingKey: String? = System.getenv("SIGNING_KEY")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")

tasks.wrapper {
    gradleVersion = "7.5.1"
    distributionType = Wrapper.DistributionType.ALL
}

plugins {
    id("com.github.ben-manes.versions") version "0.48.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.ajoberstar.grgit") version "5.2.0"
    kotlin("jvm") version "1.9.10" apply false
    idea
}

val javaVersion = JavaVersion.VERSION_11

allprojects {
    group = "ru.bozaro.gitlfs"
    version = "0.21.0-SNAPSHOT"

    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }

    repositories {
        mavenCentral()
    }
}

idea {
    project.jdkName = javaVersion.name
    project.languageLevel = IdeaLanguageLevel(javaVersion)
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    configure<JavaPluginExtension> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    val api by configurations
    val testImplementation by configurations

    dependencies {
        api("com.google.code.findbugs:jsr305:3.0.2")

        testImplementation("com.google.guava:guava:32.1.2-jre")
        testImplementation("org.testng:testng:7.8.0")
    }

    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true

            // Workaround for https://youtrack.jetbrains.com/issue/IDEA-175172
            outputDir = file("build/classes/main")
            testOutputDir = file("build/classes/test")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useTestNG {
            testLogging {
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
            }
        }
    }

    val javadoc by tasks.getting(Javadoc::class) {
        (options as? CoreJavadocOptions)?.addStringOption("Xdoclint:none", "-quiet")
    }

    val javadocJar by tasks.registering(Jar::class) {
        from(javadoc)
        archiveClassifier.set("javadoc")
    }

    val sourcesJar by tasks.registering(Jar::class) {
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])

                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                pom {
                    name.set(project.name)

                    val pomDescription = description
                    afterEvaluate {
                        pomDescription.set(project.description)
                    }

                    url.set("https://github.com/bozaro/git-lfs-java")

                    scm {
                        connection.set("scm:git:git://github.com/bozaro/git-lfs-java.git")
                        tag.set(Grgit.open(mapOf("dir" to rootDir)).head().id)
                        url.set("https://github.com/bozaro/git-lfs-java")
                    }

                    licenses {
                        license {
                            name.set("Lesser General Public License, version 3 or greater")
                            url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                        }
                    }

                    developers {
                        developer {
                            id.set("bozaro")
                            name.set("Artem V. Navrotskiy")
                            email.set("bozaro@yandex.ru")
                        }

                        developer {
                            id.set("slonopotamus")
                            name.set("Marat Radchenko")
                            email.set("marat@slonopotamus.org")
                        }
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        isRequired = signingKey != ""

        useInMemoryPgpKeys(signingKey, signingPassword)

        val publishing: PublishingExtension by project.extensions
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            packageGroup.set("ru.bozaro")
            stagingProfileId.set("365bc6dc8b7aa3")
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}
