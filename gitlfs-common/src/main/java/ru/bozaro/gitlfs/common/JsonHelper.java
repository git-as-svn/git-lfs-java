package ru.bozaro.gitlfs.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.jetbrains.annotations.NotNull;

import static com.fasterxml.jackson.core.util.DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR;

/**
 * Json utility class.
 *
 * @author Artem V. Navrotskiy
 */
public final class JsonHelper {

  @NotNull
  public static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setDateFormat(new StdDateFormat());

    // By default, pretty printer uses system newline. Explicitly configure it to use \n
    mapper.setDefaultPrettyPrinter(
        new DefaultPrettyPrinter(DEFAULT_ROOT_VALUE_SEPARATOR)
            .withObjectIndenter(new DefaultIndenter("  ", "\n")));

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  private JsonHelper() {
  }
}
