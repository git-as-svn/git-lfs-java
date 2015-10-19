package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for searialization tester.
 *
 * @author Artem V. Navrotskiy
 */
public class SerializeTester {
  public static <T> T deserialize(@NotNull String path, @NotNull Class<T> type) throws IOException {
    try (InputStream stream = SerializeTester.class.getResourceAsStream(path)) {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      Assert.assertNotNull(stream);

      final T value = mapper.readValue(stream, type);
      Assert.assertNotNull(value);

      final String json = mapper.writeValueAsString(value);
      return mapper.readValue(json, type);
    }
  }
}