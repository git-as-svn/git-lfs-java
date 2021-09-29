description = "Java Git-LFS server implementation-free library"

dependencies {
    api(project(":gitlfs-common"))
    api("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2")

    testImplementation(project(":gitlfs-client"))
    testImplementation("org.eclipse.jetty:jetty-servlet:11.0.6")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.0-alpha5")
}
