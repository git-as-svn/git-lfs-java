package ru.bozaro.gitlfs.client

import org.yaml.snakeyaml.constructor.AbstractConstruct
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.Tag
import java.nio.charset.StandardCharsets

/**
 * Constructor for more user-friendly serializing request body.
 *
 * @author Artem V. Navrotskiy
 */
class YamlConstructor : Constructor() {
    private inner class ConstructBlob : AbstractConstruct() {
        override fun construct(node: Node): Any {
            val value = constructScalar(node as ScalarNode)
            return value.toByteArray(StandardCharsets.UTF_8)
        }
    }

    private inner class ConstructBinary : AbstractConstruct() {
        override fun construct(node: Node): Any {
            val value = constructScalar(node as ScalarNode)
            return Base64Coder.decodeLines(value)
        }
    }

    init {
        yamlConstructors[Tag("!text")] = ConstructBlob()
        yamlConstructors[Tag.BINARY] = ConstructBinary()
    }
}
