package ru.bozaro.gitlfs.common.data;

import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectResTest {
  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    final ObjectRes res = SerializeTester.deserialize("object-res-01.json", ObjectRes.class);
    Assert.assertNotNull(res);

    final Meta meta = res.getMeta();
    Assert.assertNotNull(meta);
    Assert.assertEquals(meta.getOid(), "01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b");
    Assert.assertEquals(meta.getSize(), 130L);
    Assert.assertEquals(2, res.getLinks().size());

    final Link self = res.getLinks().get(LinkType.Self);
    Assert.assertNotNull(self);
    Assert.assertEquals(self.getHref(), new URI("https://storage-server.com/info/lfs/objects/01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b"));
    Assert.assertTrue(self.getHeader().isEmpty());

    final Link link = res.getLinks().get(LinkType.Upload);
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
    final ObjectRes res = SerializeTester.deserialize("object-res-02.json", ObjectRes.class);
    Assert.assertNotNull(res);
    Assert.assertNull(res.getMeta());
    Assert.assertEquals(1, res.getLinks().size());

    final Link link = res.getLinks().get(LinkType.Upload);
    Assert.assertNotNull(link);
    Assert.assertEquals(link.getHref(), new URI("https://some-upload.com"));
    Assert.assertEquals(link.getHeader(),
        ImmutableMap.builder()
            .put("Authorization", "Basic ...")
            .build()
    );
  }
}