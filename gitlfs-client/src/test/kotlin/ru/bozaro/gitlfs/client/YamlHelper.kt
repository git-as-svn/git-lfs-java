package ru.bozaro.gitlfs.client

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.introspector.BeanAccess

/**
 * Helper for YAML.
 *
 * @author Artem V. Navrotskiy
 */
object YamlHelper {
    private val YAML = createYaml()

    private fun createYaml(): Yaml {
        val options = DumperOptions()
        options.lineBreak = DumperOptions.LineBreak.UNIX
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(YamlConstructor(), YamlRepresenter(), options)
        yaml.setBeanAccess(BeanAccess.FIELD)
        return yaml
    }

    fun createReplay(resource: String): HttpReplay {
        val records: MutableList<HttpRecord> = ArrayList()
        for (item in get().loadAll(YamlHelper::class.java.getResourceAsStream(resource))) {
            records.add(item as HttpRecord)
        }
        return HttpReplay(records)
    }

    fun get(): Yaml {
        return YAML
    }
}
