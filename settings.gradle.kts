file(".").listFiles().forEach {
    if (File(it, "build.gradle.kts").exists())
        include(it.name)
}
