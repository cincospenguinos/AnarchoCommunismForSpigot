package com.lafleur.communism;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

/**
 * This is where the magic happens.
 *
 * HOW POINTS WORK:
 * - Coal: 9 pieces (or one block) yields 1 point
 * - Iron: 1 ingot yields 1 point
 * - Gold: 1 ingot yields 2 points
 * - Redstone: 21 pieces yields 2 points
 * - Diamond: 1 piece yields 10 points
 * - Emerald: 1 piece yields 20 points
 */
public class Communism implements CommandExecutor, Listener {

    private static final String COMMUNITY_CHEST_METADATA = "IS_COMMUNITY_CHEST";
    private static final FixedMetadataValue TRUE_VALUE = new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("CommunismForSpigot"), true);

    public Communism() {}

    /**
     * Handles the command. No args simply prints out the contributions score. Adding the argument "chest"
     * will cause the chest the player is looking at to become a contributions chest.
     *
     * @param commandSender - Command sender
     * @param command - What the command is (this)
     * @param s - I don't know what this is
     * @param strings - Any extra arguments
     * @return Whether or not it was consumed here.
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            // Print out the contribution score of every player
            for (Player p : Bukkit.getOnlinePlayers()) {
                try {
                    commandSender.sendMessage(p.getPlayerListName() + "     " + DBInterface.getInstance().getContributionScore(p.getPlayerListName()));
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (strings[0].equalsIgnoreCase("chest")) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                Block b = player.getTargetBlock(null, 4);

                if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
                    if (b.hasMetadata(COMMUNITY_CHEST_METADATA) || b.getMetadata(COMMUNITY_CHEST_METADATA).contains(TRUE_VALUE)) {
                        b.removeMetadata(COMMUNITY_CHEST_METADATA, Bukkit.getPluginManager().getPlugin("CommunismForSpigot"));

                        // TODO: Get inventory of chest and make player decommunity it take the blame

                        player.sendMessage(ChatColor.GREEN + "Chest is no longer community chest");
                    } else {
                        // TODO: Get inventory of chest and make player community it in the scoreboard
                        b.setMetadata(COMMUNITY_CHEST_METADATA, TRUE_VALUE);
                        player.sendMessage(ChatColor.GREEN + "Chest is now community chest");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find chest to modify!");
                }
            } else {
                commandSender.sendMessage("This command is only for players.");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Usage: /communism [chest]");
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            DBInterface.getInstance().addPlayer(event.getPlayer().getPlayerListName());
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQLException should never happen here.");
            e.printStackTrace();
            event.getPlayer().kickPlayer("A database error occurred. Try again or disable Communism plugin.");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "ClassNotFoundException should never happen here.");
            e.printStackTrace();
            event.getPlayer().kickPlayer("A database error occurred. Try again or disable Communism plugin.");
        }

        event.setJoinMessage("Welcome to Andre's Communist Server! Type \"/communism\" to see everyone's contributions.");
    }


    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            int amount = 0;

            switch(event.getItem().getItemStack().getType()) {
                case DIAMOND:
                case EMERALD:
                case COAL:
                case REDSTONE:
                case IRON_INGOT:
                case GOLD_INGOT:
                    amount = 1;
            }

            if (amount == 0)
                return;

            amount *= event.getItem().getItemStack().getAmount(); // If we pick up 3 diamonds, it should count as 30 points
            System.out.println("AMOUNT: " + amount);

            try {
                DBInterface.getInstance().incrementCollectedResource(player.getPlayerListName(), event.getItem().getItemStack().getType(), amount);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        int amount = 0;

        switch(event.getItemDrop().getItemStack().getType()) {
            case DIAMOND:
            case EMERALD:
            case COAL:
            case REDSTONE:
            case IRON_INGOT:
            case GOLD_INGOT:
                amount = 1;
        }

        if (amount == 0)
            return;

        amount *= event.getItemDrop().getItemStack().getAmount();
        try {
            DBInterface.getInstance().decrementCollectedResource(player.getPlayerListName(), event.getItemDrop().getItemStack().getType(), amount);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();

        if (event.getSource().getHolder() instanceof Player && (event.getSource().getHolder() instanceof Chest ||
                event.getSource().getHolder() instanceof DoubleChest)) {
            // Check to see if this is a community chest
            // TODO: This

            int amount = 0;

            switch(event.getItem().getType()) {
                case DIAMOND:
                case EMERALD:
                case COAL:
                case REDSTONE:
                case IRON_INGOT:
                case GOLD_INGOT:
                    amount = 1;
            }

            if (amount == 0)
                return;

            amount *= event.getItem().getAmount();


            // TODO: Decrement
        } else if (event.getDestination().getHolder() instanceof Player && event.getSource().getHolder() instanceof Furnace) {
            // TODO: Increment
        }
    }
}
