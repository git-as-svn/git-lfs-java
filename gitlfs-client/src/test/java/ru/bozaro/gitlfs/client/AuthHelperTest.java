package ru.bozaro.gitlfs.client;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * Tests for AuthHelper.
 *
 * @author Artem V. Navrotskiy
 */
public class AuthHelperTest {
  @DataProvider(name = "joinUrlProvider")
  public static Object[][] joinUrlProvider() {
    return new Object[][]{
        new Object[]{"http://test.ru/foo", "bar", "http://test.ru/foo/bar"},
        new Object[]{"http://test.ru/foo/", "bar", "http://test.ru/foo/bar"},
        new Object[]{"http://test.ru/foo", "/bar", "http://test.ru/bar"},
        new Object[]{"http://test.ru/foo/", "/bar", "http://test.ru/bar"},
        new Object[]{"https://test.ru/foo/", "http://foo.ru/bar", "http://foo.ru/bar"},
    };
  }

  @Test(dataProvider = "joinUrlProvider")
  public void joinUrl(@Nonnull String base, @Nonnull String str, @Nonnull String expected) {
    Assert.assertEquals(AuthHelper.join(URI.create(base), str), URI.create(expected));
  }
}
