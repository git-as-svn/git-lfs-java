package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * Helper for YAML.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class YamlHelper {
  @NotNull
  private static final Yaml YAML = createYaml();

  @NotNull
  public static Yaml get() {
    return YAML;
  }

  @NotNull
  private static Yaml createYaml() {
    final DumperOptions options = new DumperOptions();
    options.setLineBreak(DumperOptions.LineBreak.UNIX);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    final Yaml yaml = new Yaml(new YamlConstructor(), new YamlRepresenter(), options);
    yaml.setBeanAccess(BeanAccess.FIELD);
    return yaml;
  }
}
