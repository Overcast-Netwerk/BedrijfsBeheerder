package ch.dempsey.bedrijfsbeheerder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public static Economy eco;
	
	public boolean setupEconomy() {
		if(Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		
		if(rsp == null) {
			return false;
		}
		
		eco = rsp.getProvider();
		return eco != null;
	}
	
	
	public void onEnable() {
		if(!(setupEconomy())) getLogger().severe("COULD NOT LOAD VAULT!");
	}
	
}
