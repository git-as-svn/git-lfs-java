package ru.bozaro.gitlfs.client;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * Constructor for more user-friendly serializing request body.
 *
 * @author Artem V. Navrotskiy
 */
public class YamlConstructor extends Constructor {
  public YamlConstructor() {
    this.yamlConstructors.put(new Tag("!text"), new ConstructBlob());
    this.yamlConstructors.put(Tag.BINARY, new ConstructBinary());
  }

  private class ConstructBlob extends AbstractConstruct {
    @Nonnull
    public Object construct(@Nonnull Node node) {
      final String value = constructScalar((ScalarNode) node);
      return value.getBytes(StandardCharsets.UTF_8);
    }
  }

  private class ConstructBinary extends AbstractConstruct {
    @Nonnull
    public Object construct(@Nonnull Node node) {
      final String value = constructScalar((ScalarNode) node);
      return Base64Coder.decodeLines(value);
    }
  }
}
