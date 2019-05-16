package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Test Link deserialization.
 *
 * @author Artem V. Navrotskiy
 */
public class LinkTest {
  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    final Link link = SerializeTester.deserialize("link-01.json", Link.class);
    Assert.assertNotNull(link);
    Assert.assertEquals(link.getHref(), new URI("https://storage-server.com/OID"));
    Assert.assertEquals(link.getHeader(),
        ImmutableMap.builder()
            .put("Authorization", "Basic ...")
            .build()
    );
  }

  @Test
  public void parse02() throws IOException, ParseException, URISyntaxException {
    final Link link = SerializeTester.deserialize("link-02.json", Link.class);
    Assert.assertNotNull(link);
    Assert.assertEquals(link.getHref(), new URI("https://api.github.com/lfs/bozaro/git-lfs-java"));
    Assert.assertEquals(link.getHeader(),
        ImmutableMap.builder()
            .put("Authorization", "RemoteAuth secret")
            .build()
    );
    Assert.assertEquals(link.getExpiresAt(), StdDateFormat.instance.parse("2015-09-17T19:17:31.000+00:00"));
  }
}
