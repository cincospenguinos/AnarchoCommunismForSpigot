package com.lafleur.communism;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Main class that handles everything
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            DBInterface instance = DBInterface.getInstance();
            instance.setup();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not connect to DB! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.SEVERE, "Could not find SQL class for SQLite. Disabling...");
            getLogger().log(Level.SEVERE, "Could not connect to DB! Disabling...");
        }

        Communism communism = new Communism();
        getCommand("communism").setExecutor(communism);
        getServer().getPluginManager().registerEvents(communism, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            DBInterface.disconnect();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "SQLException when attempting to disconnect from DB!");
            e.printStackTrace();
        }
    }
}
