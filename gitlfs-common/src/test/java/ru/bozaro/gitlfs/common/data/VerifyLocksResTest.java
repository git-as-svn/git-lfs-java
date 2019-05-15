package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;

public final class VerifyLocksResTest {
  @Test
  public void parse01() throws IOException, ParseException {
    final VerifyLocksRes data = SerializeTester.deserialize("verify-locks-res-01.json", VerifyLocksRes.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getNextCursor(), "optional next ID");
    Assert.assertEquals(data.getOurs().size(), 1);

    final Lock lock = data.getOurs().get(0);
    Assert.assertNotNull(lock);
    Assert.assertEquals(lock.getId(), "some-uuid");
    Assert.assertEquals(lock.getPath(), "/path/to/file");
    Assert.assertEquals(lock.getLockedAt(), StdDateFormat.instance.parse("2016-05-17T15:49:06+00:00"));
    Assert.assertNotNull(lock.getOwner());
    Assert.assertEquals(lock.getOwner().getName(), "Jane Doe");
  }
}
