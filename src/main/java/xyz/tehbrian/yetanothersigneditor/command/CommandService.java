package xyz.tehbrian.yetanothersigneditor.command;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import dev.tehbrian.tehlib.paper.cloud.PaperCloudService;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;

import java.util.function.Function;

public class CommandService extends PaperCloudService<CommandSender> {

    private final YetAnotherSignEditor yetAnotherSignEditor;
    private final Logger logger;

    /**
     * @param yetAnotherSignEditor injected
     * @param logger               injected
     */
    @Inject
    public CommandService(
            final @NonNull YetAnotherSignEditor yetAnotherSignEditor,
            final @NonNull Logger logger
    ) {
        this.yetAnotherSignEditor = yetAnotherSignEditor;
        this.logger = logger;
    }

    /**
     * Instantiates {@link #commandManager}.
     *
     * @throws IllegalStateException if {@link #commandManager} is already instantiated
     */
    public void init() throws IllegalStateException {
        if (this.commandManager != null) {
            throw new IllegalStateException("The CommandManager is already instantiated.");
        }

        try {
            this.commandManager = new PaperCommandManager<>(
                    this.yetAnotherSignEditor,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );
        } catch (final Exception e) {
            this.logger.error("Failed to create the CommandManager.");
            this.logger.error("Disabling plugin.");
            this.logger.error("Printing stack trace, please send this to the developers:", e);
            this.yetAnotherSignEditor.disableSelf();
        }
    }

}
