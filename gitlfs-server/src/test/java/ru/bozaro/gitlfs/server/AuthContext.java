package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Operation;

/**
 * Test authenticatio context.
 *
 * @author Artem V. Navrotskiy
 */
public class AuthContext {
  @NotNull
  private final Operation operation;

  public AuthContext(@NotNull Operation operation) {
    this.operation = operation;
  }

  @NotNull
  public Operation getOperation() {
    return operation;
  }
}
