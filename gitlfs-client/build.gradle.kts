description = "Java Git-LFS client library"

dependencies {
    api(project(":gitlfs-common"))
    api("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.slf4j:slf4j-api:1.7.36")

    testImplementation("org.yaml:snakeyaml:1.33")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.36")
}
