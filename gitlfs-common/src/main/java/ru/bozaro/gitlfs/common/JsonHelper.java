package ru.bozaro.gitlfs.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;

import static com.fasterxml.jackson.core.util.DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR;

/**
 * Json utility class.
 *
 * @author Artem V. Navrotskiy
 * @author Marat Radchenko <marat@slonopootamus.org>
 */
public final class JsonHelper {

  /**
   * git-lfs is broken and doesn't properly parse output of {@link com.fasterxml.jackson.databind.util.StdDateFormat}.
   * <p/>
   * See https://github.com/git-lfs/git-lfs/issues/3660
   */
  @NotNull
  public static final DateFormat dateFormat = new ISO8601DateFormat();

  @NotNull
  public static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setDateFormat(dateFormat);

    // By default, pretty printer uses system newline. Explicitly configure it to use \n
    mapper.setDefaultPrettyPrinter(
        new DefaultPrettyPrinter(DEFAULT_ROOT_VALUE_SEPARATOR)
            .withObjectIndenter(new DefaultIndenter("  ", "\n")));

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  private JsonHelper() {
  }
}
