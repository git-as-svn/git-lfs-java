package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;

public final class LocksResTest {
  @Test
  public void parse01() throws IOException, ParseException {
    final LocksRes data = SerializeTester.deserialize("locks-res-01.json", LocksRes.class);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getNextCursor(), "optional next ID");
    Assert.assertEquals(data.getLocks().size(), 1);

    final Lock lock = data.getLocks().get(0);
    Assert.assertNotNull(lock);
    Assert.assertEquals(lock.getId(), "some-uuid");
    Assert.assertEquals(lock.getPath(), "/path/to/file");
    Assert.assertEquals(lock.getLockedAt(), StdDateFormat.instance.parse("2016-05-17T15:49:06+00:00"));
    Assert.assertNotNull(lock.getOwner());
    Assert.assertEquals(lock.getOwner().getName(), "Jane Doe");
  }
}
