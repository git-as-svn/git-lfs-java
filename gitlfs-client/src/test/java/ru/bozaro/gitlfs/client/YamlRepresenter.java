package ru.bozaro.gitlfs.client;

import com.google.common.base.Utf8;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.nio.charset.StandardCharsets;

/**
 * Representer for more user-friendly serializing request body.
 *
 * @author Artem V. Navrotskiy
 */
public class YamlRepresenter extends Representer {
  public YamlRepresenter() {
    this.representers.put(byte[].class, new RepresentBlob());
  }

  private class RepresentBlob implements Represent {
    public Node representData(Object data) {
      byte[] value = (byte[]) data;
      if (Utf8.isWellFormed(value)) {
        return representScalar(new Tag("!text"), new String(value, StandardCharsets.UTF_8), DumperOptions.ScalarStyle.LITERAL);
      } else {
        String binary = Base64Coder.encodeLines((byte[]) data);
        return representScalar(Tag.BINARY, binary, DumperOptions.ScalarStyle.LITERAL);
      }
    }
  }
}
