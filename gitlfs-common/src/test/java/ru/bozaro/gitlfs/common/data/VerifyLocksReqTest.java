package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public final class VerifyLocksReqTest {
  @Test
  public void parse01() throws IOException {
    final VerifyLocksReq data = SerializeTester.deserialize("verify-locks-req-01.json", VerifyLocksReq.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getCursor(), "optional cursor");
    Assert.assertEquals((Integer) 100, data.getLimit());
    Assert.assertNotNull(data.getRef());
    Assert.assertEquals(data.getRef().getName(), "refs/heads/my-feature");
  }
}
