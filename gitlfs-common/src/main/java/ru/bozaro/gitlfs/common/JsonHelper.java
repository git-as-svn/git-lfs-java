package ru.bozaro.gitlfs.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.jetbrains.annotations.NotNull;

/**
 * Json utility class.
 *
 * @author Artem V. Navrotskiy
 */
public final class JsonHelper {
  private JsonHelper() {
  }

  /**
   * Creating mapper for serialize/deserialize data to JSON.
   */
  @NotNull
  public static ObjectMapper createMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setDateFormat(new ISO8601DateFormat());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  /**
   * Convert object to string.
   *
   * @param data Object.
   * @return JSON data.
   */
  @NotNull
  public static String toString(@NotNull Object data) throws JsonProcessingException {
    return createMapper().writeValueAsString(data);
  }
}
