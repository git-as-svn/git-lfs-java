package ru.bozaro.gitlfs.client

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import ru.bozaro.gitlfs.client.AuthHelper.join
import java.net.URI

/**
 * Tests for AuthHelper.
 *
 * @author Artem V. Navrotskiy
 */
class AuthHelperTest {
    @Test(dataProvider = "joinUrlProvider")
    fun joinUrl(base: String, str: String, expected: String) {
        Assert.assertEquals(join(URI.create(base), str), URI.create(expected))
    }

    @DataProvider(name = "joinUrlProvider")
    fun joinUrlProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("http://test.ru/foo", "bar", "http://test.ru/foo/bar"),
                arrayOf("http://test.ru/foo/", "bar", "http://test.ru/foo/bar"),
                arrayOf("http://test.ru/foo", "/bar", "http://test.ru/bar"),
                arrayOf("http://test.ru/foo/", "/bar", "http://test.ru/bar"),
                arrayOf("https://test.ru/foo/", "http://foo.ru/bar", "http://foo.ru/bar")
        )
    }
}
