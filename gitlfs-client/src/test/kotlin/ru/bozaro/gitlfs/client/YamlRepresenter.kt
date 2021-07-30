package ru.bozaro.gitlfs.client

import com.google.common.base.Utf8
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer
import java.nio.charset.StandardCharsets

/**
 * Representer for more user-friendly serializing request body.
 *
 * @author Artem V. Navrotskiy
 */
class YamlRepresenter : Representer() {
    private inner class RepresentBlob : Represent {
        override fun representData(data: Any): Node {
            val value = data as ByteArray
            return if (Utf8.isWellFormed(value)) {
                representScalar(Tag("!text"), String(value, StandardCharsets.UTF_8), DumperOptions.ScalarStyle.LITERAL)
            } else {
                val binary = Base64Coder.encodeLines(data)
                representScalar(Tag.BINARY, binary, DumperOptions.ScalarStyle.LITERAL)
            }
        }
    }

    init {
        representers[ByteArray::class.java] = RepresentBlob()
    }
}
