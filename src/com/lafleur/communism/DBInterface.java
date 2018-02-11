package com.lafleur.communism;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.*;

public class DBInterface {

    private static DBInterface instance;

    private static final String DB_FILE_NAME = "communism.db";
    private static final String RESOURCES_COLLECTED_TABLE_NAME = "collected_resources";
    private static final String CONTRIBUTIONS_TABLE_NAME = "contributions";

    private Connection connection;

    private DBInterface() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_NAME);
        Bukkit.getLogger().info("Connected to DB!");
    }

    public static DBInterface getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null)
            instance = new DBInterface();

        return instance;
    }

    public static void disconnect() throws SQLException {
        instance.connection.close();
        instance = null;
    }

    public void setup() throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS " + RESOURCES_COLLECTED_TABLE_NAME + " (name STRING PRIMARY KEY, " +
                "coal INT, iron INT, redstone INT, gold INT, diamond INT, emerald INT)";
        statement.executeUpdate(sql);

        sql = "CREATE TABLE IF NOT EXISTS " + CONTRIBUTIONS_TABLE_NAME + " (name STRING PRIMARY KEY, " +
                "coal INT, iron INT, redstone INT, gold INT, diamond INT, emerald INT)";
        statement.executeUpdate(sql);
    }

    public void addPlayer(String playerName) throws SQLException {
        String sql = "INSERT OR IGNORE INTO " + RESOURCES_COLLECTED_TABLE_NAME + "(name, coal, iron, redstone, gold, diamond, emerald) VALUES (?,0,0,0,0,0,0)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, playerName);
        preparedStatement.executeUpdate();

        sql = "INSERT OR IGNORE INTO " + CONTRIBUTIONS_TABLE_NAME + "(name, coal, iron, redstone, gold, diamond, emerald) VALUES (?, 0, 0, 0, 0, 0, 0)";
        PreparedStatement preparedStatement2 = connection.prepareStatement(sql);
        preparedStatement2.setString(1, playerName);
        preparedStatement2.executeUpdate();
    }

    public void incrementContribution(String playerName, Material blockType, int amount) {
        // TODO: This
    }

    public void decrementContribution(String playerName, Material blockType, int amount) {
        // TODO: This
    }

    public double getContributionScore(String playerName) throws SQLException {
        int gathered = 0;
        int given = 0;

        String query = "SELECT * FROM " + RESOURCES_COLLECTED_TABLE_NAME + " WHERE name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, playerName);
        ResultSet results = statement.executeQuery();
        if (!results.next())
            System.out.println("No players found!");

        for (int i = 2; i <= 7; i++)
            gathered += results.getInt(i);

        results.close();
        statement.close();

        query = "SELECT * FROM " + CONTRIBUTIONS_TABLE_NAME + " WHERE name = ?";
        PreparedStatement statement2 = connection.prepareStatement(query);
        statement2.setString(1, playerName);
        ResultSet results2 = statement2.executeQuery();
        results2.next();

        for (int i = 2; i <= 7; i++)
            given += results2.getInt(i);

        results2.close();
        statement2.close();

        if (given == 0 && gathered == 0) // To avoid divide-by-zero error
            return 1.0;

        return (double)given / (double)gathered;
    }
}
