package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy
 */
public class MetaTest {
  @Test
  public void parse01() throws IOException, ParseException, URISyntaxException {
    final Meta meta = SerializeTester.deserialize("meta-01.json", Meta.class);
    Assert.assertNotNull(meta);
    Assert.assertEquals(meta.getOid(), "01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b");
    Assert.assertEquals(meta.getSize(), 130L);
  }
}
