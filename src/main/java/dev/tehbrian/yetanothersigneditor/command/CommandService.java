package dev.tehbrian.yetanothersigneditor.command;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import dev.tehbrian.tehlib.paper.cloud.PaperCloudService;
import dev.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import org.bukkit.command.CommandSender;

import java.util.function.Function;

public final class CommandService extends PaperCloudService<CommandSender> {

  private final YetAnotherSignEditor yetAnotherSignEditor;

  @Inject
  public CommandService(
      final YetAnotherSignEditor yetAnotherSignEditor
  ) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
  }

  /**
   * Instantiates {@link #commandManager}.
   *
   * @throws IllegalStateException if {@link #commandManager} is already instantiated
   * @throws Exception             if something goes wrong during instantiation
   */
  public void init() throws Exception {
    if (this.commandManager != null) {
      throw new IllegalStateException("The CommandManager is already instantiated.");
    }

    this.commandManager = new PaperCommandManager<>(
        this.yetAnotherSignEditor,
        CommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(),
        Function.identity()
    );
  }

}
