package ru.bozaro.gitlfs.client;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for YAML.
 *
 * @author Artem V. Navrotskiy
 */
public class YamlHelper {
  @Nonnull
  private static final Yaml YAML = createYaml();

  @Nonnull
  private static Yaml createYaml() {
    final DumperOptions options = new DumperOptions();
    options.setLineBreak(DumperOptions.LineBreak.UNIX);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    final Yaml yaml = new Yaml(new YamlConstructor(), new YamlRepresenter(), options);
    yaml.setBeanAccess(BeanAccess.FIELD);
    return yaml;
  }

  @Nonnull
  public static HttpReplay createReplay(@Nonnull String resource) {
    final List<HttpRecord> records = new ArrayList<>();
    for (Object item : YamlHelper.get().loadAll(YamlHelper.class.getResourceAsStream(resource))) {
      records.add((HttpRecord) item);
    }
    return new HttpReplay(records);
  }

  @Nonnull
  public static Yaml get() {
    return YAML;
  }
}
