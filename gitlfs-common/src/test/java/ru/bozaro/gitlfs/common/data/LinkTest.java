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
import java.util.Map;
import java.util.TreeMap;

/**
 * Test Link deserialization.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class LinkTest {
  @NotNull
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    try (InputStream stream = getClass().getResourceAsStream("link-01.json")) {
      Assert.assertNotNull(stream);

      final Auth auth = mapper.readValue(stream, Auth.class);
      Assert.assertNotNull(auth);
      Assert.assertEquals(auth.getHref(), new URI("https://storage-server.com/OID"));
      Assert.assertEquals(auth.getHeader(),
          ImmutableMap.builder()
              .put("Authorization", "Basic ...")
              .build()
      );
    }
  }
}
