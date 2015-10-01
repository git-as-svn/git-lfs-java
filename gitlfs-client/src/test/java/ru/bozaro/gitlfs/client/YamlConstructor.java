package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.nio.charset.StandardCharsets;

/**
 * Constructor for more user-friendly serializing request body.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class YamlConstructor extends Constructor {
  public YamlConstructor() {
    this.yamlConstructors.put(new Tag("!text"), new ConstructBlob());
    this.yamlConstructors.put(Tag.BINARY, new ConstructBinary());
  }

  private class ConstructBlob extends AbstractConstruct {
    @NotNull
    public Object construct(@NotNull Node node) {
      final String value = constructScalar((ScalarNode) node).toString();
      return value.getBytes(StandardCharsets.UTF_8);
    }
  }

  private class ConstructBinary extends AbstractConstruct {
    @NotNull
    public Object construct(@NotNull Node node) {
      final String value = constructScalar((ScalarNode) node).toString();
      return Base64Coder.decodeLines(value);
    }
  }
}
