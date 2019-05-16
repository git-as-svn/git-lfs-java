package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public final class DeleteLockReqTest {
  @Test
  public void parse01() throws IOException {
    final DeleteLockReq data = SerializeTester.deserialize("delete-lock-req-01.json", DeleteLockReq.class);
    Assert.assertNotNull(data);
    Assert.assertTrue(data.isForce());

    Assert.assertNotNull(data.getRef());
    Assert.assertEquals(data.getRef().getName(), "refs/heads/my-feature");
  }
}
