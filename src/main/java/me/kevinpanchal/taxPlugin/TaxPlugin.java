package me.kevinpanchal.taxPlugin;

import org.bukkit.plugin.java.*;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

public final class TaxPlugin extends JavaPlugin {

    private static Economy economy = null;
    private double taxRate;
    private long taxInterval;
    private double minimumBalance;

    @Override
    public void onEnable() {
        // Load the configuration file
        saveDefaultConfig();
        loadConfigValues();

        // Ensure Vault is present
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("TaxPlugin has been enabled!");

        // Start the tax collection system
        startTaxCollection();
    }

    @Override
    public void onDisable() {
        getLogger().info("TaxPlugin has been disabled.");
    }

    // Load configuration values
    private void loadConfigValues() {
        taxRate = getConfig().getDouble("tax-rate");
        taxInterval = getConfig().getLong("tax-interval") * 60L * 20L;  // Convert minutes to ticks
        minimumBalance = getConfig().getDouble("minimum-balance");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return economy != null;
    }

    // Function to start periodic tax collection
    private void startTaxCollection() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double balance = economy.getBalance(player);
                    if (balance >= minimumBalance) {
                        double tax = balance * taxRate;
                        if (balance >= tax) {
                            economy.withdrawPlayer(player, tax);
                            player.sendMessage("§eTax of §6" + tax + "§e has been deducted from your balance.");
                        } else {
                            player.sendMessage("§cYou don't have enough money to pay the tax.");
                        }
                    } else {
                        player.sendMessage("§cYour balance is too low to be taxed.");
                    }
                }
            }
        }.runTaskTimer(this, 0L, taxInterval);  // Use dynamic tax interval from config
    }
}


