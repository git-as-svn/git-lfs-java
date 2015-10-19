package ru.bozaro.gitlfs.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
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
