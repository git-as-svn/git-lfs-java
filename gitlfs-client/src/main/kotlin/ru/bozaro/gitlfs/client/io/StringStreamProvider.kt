package ru.bozaro.gitlfs.client.io

import java.nio.charset.StandardCharsets

/**
 * Create stream from string.
 *
 * @author Artem V. Navrotskiy
 */
class StringStreamProvider(data: String) : ByteArrayStreamProvider(data.toByteArray(StandardCharsets.UTF_8))
