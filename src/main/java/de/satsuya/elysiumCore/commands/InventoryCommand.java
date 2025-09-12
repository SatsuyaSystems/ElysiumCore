package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.manager.ItemManager;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@PluginCommand(name = "inventory")
public class InventoryCommand implements CommandExecutor, TabExecutor {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        Inventory inventory = Bukkit.createInventory(null, 54, "Elysium Inventory");

        List<ItemStack> allCustomItems = loadAllItemsDynamically();

        for (ItemStack item : allCustomItems) {
            inventory.addItem(item);
        }

        player.openInventory(inventory);
        return true;
    }

    private List<ItemStack> loadAllItemsDynamically() {
        List<ItemStack> items = new ArrayList<>();

        // Get the ItemManager class object
        Class<ItemManager> itemManagerClass = ItemManager.class;

        // Get all declared methods in the class
        Method[] methods = itemManagerClass.getDeclaredMethods();

        for (Method method : methods) {
            // Check if the method is public and static
            if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {

                // Check if the method returns an ItemStack
                if (method.getReturnType().equals(ItemStack.class)) {

                    // Check if the method has no parameters
                    if (method.getParameterCount() == 0) {
                        try {
                            // Invoke the static method to get the ItemStack
                            ItemStack item = (ItemStack) method.invoke(null);
                            if (item != null) {
                                items.add(item);
                            }
                        } catch (Exception e) {
                            // In a real plugin, you would log this error
                            ElysiumLogger.error("Failed to invoke method " + method.getName() + " in ItemManager.");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return items;
    }

}
