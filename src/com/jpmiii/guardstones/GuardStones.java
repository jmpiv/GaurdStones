package com.jpmiii.guardstones;

import java.util.HashMap;


import org.bukkit.Material;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.jpmiii.guardstones.GuardStonesListen;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class GuardStones extends JavaPlugin {
	
	
	
	// Fired when plugin is first enabled
	public static Flag LIFE = new IntegerFlag("life");
	public static Flag SOURCEX = new IntegerFlag("sourcex");
	public static Flag SOURCEY = new IntegerFlag("sourcey");
	public static Flag SOURCEZ = new IntegerFlag("sourcez");
	public static Flag CREATED = new IntegerFlag("created");
	public static Flag NLGROUP = new StringFlag("nlgroup");
	public static Flag OTHER = new StringFlag("other");
	FileConfiguration config = getConfig();
	public HashMap<Material, String[]> stoneList = new HashMap<Material, String[]>();
	
	
	
	
	@Override
	public void onEnable() {

		getServer().getPluginManager().registerEvents(new GuardStonesListen(this), this);
		this.saveDefaultConfig();
		//getLogger().info("onEnable has been invoked!");

		for (String str : getConfig().getStringList("Stones")) {
			String s[] = str.split(", ");

			stoneList.put(Material.getMaterial(s[0]), s);
		}
        //getServer().getWorld(this.getConfig().getString("worldName")).setMonsterSpawnLimit(this.getConfig().getInt("mobsPerChunk"));
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable() {

	}
	@Override
	public void onLoad() {
		FlagRegistry registry = WorldGuardPlugin.inst().getFlagRegistry();
	    try {
	        registry.register(LIFE);
	    } catch (FlagConflictException e) {
	        LIFE = registry.get("life"); // non-final!
	    }
	    try {
	        registry.register(SOURCEX);
	    } catch (FlagConflictException e) {
	        SOURCEX = registry.get("sourcex"); // non-final!
	    }
	    try {
	        registry.register(SOURCEY);
	    } catch (FlagConflictException e) {
	        SOURCEY = registry.get("sourcey"); // non-final!
	    }
	    try {
	        registry.register(SOURCEZ);
	    } catch (FlagConflictException e) {
	        SOURCEZ = registry.get("sourcez"); // non-final!
	    }
	    try {
	        registry.register(CREATED);
	    } catch (FlagConflictException e) {
	        CREATED = registry.get("created"); // non-final!
	    }
	    try {
	        registry.register(NLGROUP);
	    } catch (FlagConflictException e) {
	    	NLGROUP = registry.get("nlgroup"); // non-final!
	    }
	    try {
	        registry.register(OTHER);
	    } catch (FlagConflictException e) {
	    	OTHER = registry.get("OTHER"); // non-final!
	    }
	    
	}
}