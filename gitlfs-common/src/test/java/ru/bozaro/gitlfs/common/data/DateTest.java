package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.common.JsonHelper;

import java.text.ParseException;

public final class DateTest {

  @Test
  public void format() throws ParseException {
    final String str = "2006-01-02T15:04:05.123+00:00";
    Assert.assertEquals(JsonHelper.dateFormat.format(JsonHelper.dateFormat.parse(str)), str);
  }
}
