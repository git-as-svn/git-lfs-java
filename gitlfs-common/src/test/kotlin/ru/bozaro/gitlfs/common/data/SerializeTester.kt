package ru.bozaro.gitlfs.common.data

import org.testng.Assert
import ru.bozaro.gitlfs.common.JsonHelper

/**
 * Class for searialization tester.
 *
 * @author Artem V. Navrotskiy
 */
object SerializeTester {
    fun <T> deserialize(path: String?, type: Class<T>?): T {
        SerializeTester::class.java.getResourceAsStream(path).use { stream ->
            Assert.assertNotNull(stream)
            val value: T = JsonHelper.mapper.readValue(stream, type)
            Assert.assertNotNull(value)
            val json: String = JsonHelper.mapper.writeValueAsString(value)
            return JsonHelper.mapper.readValue(json, type)
        }
    }
}
