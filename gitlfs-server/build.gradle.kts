description = "Java Git-LFS server implementation-free library"

dependencies {
    compile(project(":gitlfs-common"))
    compile("javax.servlet:javax.servlet-api:4.0.1")

    testCompile(project(":gitlfs-client"))
    testCompile("org.eclipse.jetty:jetty-servlet:9.4.20.v20190813")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.28")
}
