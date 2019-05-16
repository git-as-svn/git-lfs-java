package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public final class CreateLockReqTest {
  @Test
  public void parse01() throws IOException {
    final CreateLockReq data = SerializeTester.deserialize("create-lock-req-01.json", CreateLockReq.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getPath(), "foo/bar.zip");

    Assert.assertNotNull(data.getRef());
    Assert.assertEquals(data.getRef().getName(), "refs/heads/my-feature");
  }
}
