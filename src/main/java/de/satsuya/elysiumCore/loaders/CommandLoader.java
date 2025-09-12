package de.satsuya.elysiumCore.loaders;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class CommandLoader {

    public static void loadCommands(JavaPlugin plugin) {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.commands");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(PluginCommand.class);

        for (Class<?> clazz : commandClasses) {
            if (Modifier.isAbstract(clazz.getModifiers())) continue;
            if (!CommandExecutor.class.isAssignableFrom(clazz)) {
                ElysiumLogger.error("Class " + clazz.getName() + " is annotated with @PluginCommand but does not implement CommandExecutor. Skipping.");
                continue;
            }
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                CommandExecutor executor = (CommandExecutor) instance;

                PluginCommand annotation = clazz.getAnnotation(PluginCommand.class);
                String commandName = annotation.name();

                if (plugin.getCommand(commandName) != null) {
                    plugin.getCommand(commandName).setExecutor(executor);
                    if (executor instanceof TabExecutor) {
                        plugin.getCommand(commandName).setTabCompleter((TabExecutor) executor);
                    }
                    ElysiumLogger.log("Command " + commandName + " loaded successfully.");
                } else {
                    ElysiumLogger.error("Command '" + commandName + "' is not registered in plugin.yml. Skipping.");
                }
            } catch (Exception e) {
                ElysiumLogger.error("Failed to load command from " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}