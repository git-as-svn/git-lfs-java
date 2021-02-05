description = "Java Git-LFS server implementation-free library"

dependencies {
    api(project(":gitlfs-common"))
    api("javax.servlet:javax.servlet-api:4.0.1")

    testImplementation(project(":gitlfs-client"))
    testImplementation("org.eclipse.jetty:jetty-servlet:9.4.36.v20210114")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.30")
}
