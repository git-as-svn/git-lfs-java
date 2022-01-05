description = "Java Git-LFS client library"

dependencies {
    api(project(":gitlfs-common"))
    api("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")

    testImplementation("org.yaml:snakeyaml:1.30")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.0-alpha5")
}
