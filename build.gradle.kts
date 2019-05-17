import com.github.benmanes.gradle.versions.VersionsPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
val gitCommit = System.getenv("TRAVIS_COMMIT") ?: ""

tasks.wrapper {
    gradleVersion = "5.4"
    distributionType = Wrapper.DistributionType.ALL
}

plugins {
    id("com.github.ben-manes.versions") version "0.21.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0"
    idea
}

allprojects {
    apply<IdeaPlugin>()
    apply<VersionsPlugin>()

    repositories {
        mavenCentral()
    }
}

val javaVersion = JavaVersion.VERSION_1_8

idea {
    project.jdkName = javaVersion.name
}

subprojects {
    group = "ru.bozaro.gitlfs"
    version = "0.13.0-SNAPSHOT"

    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    configure<JavaPluginExtension> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    val compile by configurations
    val testCompile by configurations

    dependencies {
        compile("org.jetbrains:annotations:17.0.0")

        testCompile("com.google.guava:guava:27.1-jre")
        testCompile("org.testng:testng:6.14.3")
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

    val javadocJar by tasks.creating(Jar::class) {
        from(javadoc)
        archiveClassifier.set("javadoc")
    }

    val sourcesJar by tasks.creating(Jar::class) {
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    url.set("https://github.com/bozaro/git-lfs-java")

                    scm {
                        connection.set("scm:git:git://github.com/bozaro/git-lfs-java.git")
                        tag.set(gitCommit)
                        url.set("https://github.com/bozaro/git-lfs-java")
                    }

                    licenses {
                        license {
                            name.set("Lesser General Public License, version 3 or greater")
                            url.set("http://www.gnu.org/licenses/lgpl.html")
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

    val secretKeyRingFile = "${rootProject.projectDir}/secring.gpg"
    extra["signing.secretKeyRingFile"] = secretKeyRingFile
    extra["signing.keyId"] = "4B49488E"
    extra["signing.password"] = signingPassword

    configure<SigningExtension> {
        isRequired = signingPassword != null && file(secretKeyRingFile).exists()

        val publishing: PublishingExtension by project.extensions
        sign(publishing.publications)
    }
}

nexusPublishing {
    username.set(ossrhUsername)
    password.set(ossrhPassword)
}
