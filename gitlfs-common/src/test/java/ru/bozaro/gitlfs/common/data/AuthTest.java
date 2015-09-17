package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Test Auth deserialization.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class AuthTest {
  @NotNull
  private final ObjectMapper mapper = new ObjectMapper();
  @NotNull
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    try (InputStream stream = getClass().getResourceAsStream("auth-01.json")) {
      Assert.assertNotNull(stream);

      final Auth auth = mapper.readValue(stream, Auth.class);
      Assert.assertNotNull(auth);
      Assert.assertEquals(auth.getHref(), new URI("https://api.github.com/lfs/bozaro/git-lfs-java"));
      Assert.assertEquals(auth.getHeader(),
          ImmutableMap.builder()
              .put("Authorization", "RemoteAuth secret")
              .build()
      );
      Assert.assertEquals(auth.getExpiresAt(), dateFormat.parse("2015-09-17T19:17:31.000+00:00"));
    }
  }
}
