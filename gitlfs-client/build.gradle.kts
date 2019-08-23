description = "Java Git-LFS client library"

dependencies {
    compile(project(":gitlfs-common"))
    compile("org.apache.httpcomponents:httpclient:4.5.8")
    compile("org.slf4j:slf4j-api:1.7.28")

    testCompile("org.yaml:snakeyaml:1.24")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.28")
}
