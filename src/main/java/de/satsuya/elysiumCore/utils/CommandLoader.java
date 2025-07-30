package de.satsuya.elysiumCore.utils;

import de.satsuya.elysiumCore.commands.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class CommandLoader {

    public static void loadCommands(JavaPlugin plugin) {
        Reflections reflections = new Reflections("de.satsuya.elysiumCore.commands");
        Set<Class<? extends PluginCommand>> commandClasses = reflections.getSubTypesOf(PluginCommand.class);

        for (Class<? extends PluginCommand> clazz : commandClasses) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    PluginCommand executor = clazz.getDeclaredConstructor().newInstance();
                    String commandName = executor.getName();
                    if (plugin.getCommand(commandName) != null) {
                        plugin.getCommand(commandName).setExecutor(executor);
                        ElysiumLogger.log("Command " + commandName + " loaded successfully.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}