package ru.bozaro.gitlfs.common.data;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Test Link deserialization.
 *
 * @author Artem V. Navrotskiy
 */
public class LinkTest {
  @NotNull
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

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
    Assert.assertEquals(link.getExpiresAt(), dateFormat.parse("2015-09-17T19:17:31.000+00:00"));
  }
}
