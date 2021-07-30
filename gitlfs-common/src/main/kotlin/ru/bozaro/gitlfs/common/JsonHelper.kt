package ru.bozaro.gitlfs.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import java.text.DateFormat

/**
 * Json utility class.
 *
 * @author Artem V. Navrotskiy
 * @author Marat Radchenko <marat></marat>@slonopotamus.org>
 */
object JsonHelper {
    /**
     * git-lfs cannot parse timezone without colon: [com.fasterxml.jackson.databind.util.StdDateFormat].
     *
     *
     * See https://github.com/git-lfs/git-lfs/issues/3660
     */
    val dateFormat: DateFormat = StdDateFormat.instance.withColonInTimeZone(true)

    val mapper = ObjectMapper()

    init {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapper.dateFormat = dateFormat

        // By default, pretty printer uses system newline. Explicitly configure it to use \n
        mapper.setDefaultPrettyPrinter(
                DefaultPrettyPrinter(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR)
                        .withObjectIndenter(DefaultIndenter("  ", "\n")))
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
