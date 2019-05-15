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
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchResTest {
  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    final BatchRes data = SerializeTester.deserialize("batch-res-01.json", BatchRes.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(2, data.getObjects().size());
    {
      final BatchItem item = data.getObjects().get(0);
      Assert.assertNotNull(item);
      Assert.assertEquals(item.getOid(), "1111111");
      Assert.assertEquals(item.getSize(), 123L);

      Assert.assertNull(item.getError());

      Assert.assertEquals(1, item.getLinks().size());
      final Link link = item.getLinks().get(LinkType.Download);
      Assert.assertNotNull(link);
      Assert.assertEquals(link.getHref(), new URI("https://some-download.com"));
      Assert.assertEquals(link.getHeader(),
          ImmutableMap.builder()
              .put("Authorization", "Basic ...")
              .build()
      );
      Assert.assertEquals(link.getExpiresAt(), StdDateFormat.instance.parse("2015-07-27T21:15:01.000+00:00"));
    }
    {
      final BatchItem item = data.getObjects().get(1);
      Assert.assertNotNull(item);
      Assert.assertEquals(item.getOid(), "2222222");
      Assert.assertEquals(item.getSize(), 234L);

      Assert.assertTrue(item.getLinks().isEmpty());

      final Error error = item.getError();
      Assert.assertNotNull(error);
      Assert.assertEquals(error.getCode(), 404);
      Assert.assertEquals(error.getMessage(), "Object does not exist on the server");
    }
  }
}
