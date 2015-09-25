package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class BatchReqTest {
  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    final BatchReq data = SerializeTester.deserialize("batch-req-01.json", BatchReq.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getOperation(), Operation.Upload);

    Assert.assertEquals(1, data.getObjects().size());
    final Meta meta = data.getObjects().get(0);
    Assert.assertNotNull(meta);
    Assert.assertEquals(meta.getOid(), "1111111");
    Assert.assertEquals(meta.getSize(), 123L);
  }
}
