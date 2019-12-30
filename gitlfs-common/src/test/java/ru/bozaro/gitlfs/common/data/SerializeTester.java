package ru.bozaro.gitlfs.common.data;

import org.testng.Assert;
import ru.bozaro.gitlfs.common.JsonHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for searialization tester.
 *
 * @author Artem V. Navrotskiy
 */
public class SerializeTester {
  public static <T> T deserialize(@Nonnull String path, @Nonnull Class<T> type) throws IOException {
    try (InputStream stream = SerializeTester.class.getResourceAsStream(path)) {
      Assert.assertNotNull(stream);

      final T value = JsonHelper.mapper.readValue(stream, type);
      Assert.assertNotNull(value);

      final String json = JsonHelper.mapper.writeValueAsString(value);
      return JsonHelper.mapper.readValue(json, type);
    }
  }
}
