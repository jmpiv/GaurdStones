package com.jpmiii.guardstones;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;

import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.group.Group;

public class GuardStonesListen implements Listener {

	public static HashMap<Player, Long> placePlayer = new HashMap<Player, Long>();

	private GuardStones plugin;

	public GuardStonesListen(GuardStones plugin) {
		this.plugin = plugin;
	}
	

	RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
	ReinforcementManager blockReinforce = vg.civcraft.mc.citadel.Citadel.getReinforcementManager();

	@EventHandler
	public void reinforce(ReinforcementCreationEvent event) {
		this.plugin.getLogger()
				.info(event.getPlayer().getDisplayName() + " " + event.getReinforcement().getDurability());
		LocalPlayer p = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
		ApplicableRegionSet set = getSet(event.getBlock().getLocation());
		if (set.size() > 0) {
			for (ProtectedRegion region : set) {
				if (!isMember(event.getPlayer(), region)) {
					event.getPlayer().sendMessage("you can not reinforce here");
					event.setCancelled(true);
				}

			}
		} else if (this.plugin.stoneList.containsKey(event.getBlock().getType())) {
			if (event.getReinforcement() instanceof PlayerReinforcement) {

				String[] arg = this.plugin.stoneList.get(event.getBlock().getType());


				BlockVector min;
				BlockVector max;
				if (Integer.parseInt(arg[2]) == 0) {
					min = BlockVector.toBlockPoint(event.getBlock().getLocation().getX() + Integer.parseInt(arg[1]), 0,
							event.getBlock().getLocation().getZ() + Integer.parseInt(arg[3]));
					max = BlockVector.toBlockPoint(event.getBlock().getLocation().getX() - Integer.parseInt(arg[1]),
							255, event.getBlock().getLocation().getZ() - Integer.parseInt(arg[3]));
				} else {
					min = BlockVector.toBlockPoint(event.getBlock().getLocation().getX() + Integer.parseInt(arg[1]),
							event.getBlock().getLocation().getY() + Integer.parseInt(arg[2]),
							event.getBlock().getLocation().getZ() + Integer.parseInt(arg[3]));
					max = BlockVector.toBlockPoint(event.getBlock().getLocation().getX() - Integer.parseInt(arg[1]),
							event.getBlock().getLocation().getY() - Integer.parseInt(arg[2]),
							event.getBlock().getLocation().getZ() - Integer.parseInt(arg[3]));
				}

				ProtectedRegion region = new ProtectedCuboidRegion(UUID.randomUUID().toString(), min, max);
				
				
				
				
				
//				RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//				RegionManager regions = container.get(world);
//				if (regions != null) {
//				    return regions.getRegion("spawn");
//				}
				
				
				
				RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
				RegionManager regions = container.get(event.getBlock().getWorld());
				event.getPlayer().sendMessage("Field Overlap - " + regions.getApplicableRegions(region).size());
				if (regions.getApplicableRegions(region).size() > 0) {
					event.getPlayer().sendMessage("Field Overlap");
					return;
				}
				ItemStack cost = new ItemStack(Material.getMaterial(arg[5]), Integer.parseInt(arg[6]));
				if (event.getPlayer().getInventory().contains(cost)) {
					event.getPlayer().getInventory().remove(cost);
					this.plugin.getLogger().info(cost.toString());
				} else {
					return;
				}

				regions.addRegion(region);
				this.plugin.getLogger().info(region.getId());
				region.setFlag(GuardStones.SOURCEX, event.getBlock().getX());
				region.setFlag(GuardStones.SOURCEY, event.getBlock().getY());
				region.setFlag(GuardStones.SOURCEZ, event.getBlock().getZ());
				int cre = (int) (System.currentTimeMillis() / 3600000);
				region.setFlag(GuardStones.LIFE, cre + Integer.parseInt(arg[4]));
				region.setFlag(GuardStones.CREATED, cre);
				region.setFlag(GuardStones.OTHER,arg[7]);
				region.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
				region.setFlag(GuardStones.NLGROUP, ((PlayerReinforcement) event.getReinforcement()).getGroup().getName());
				event.getPlayer().sendMessage("Field Up");
			}
		}
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
	public void onElevator(PlayerInteractEvent event) {
		if (event.hasBlock()) {

			if (event.getClickedBlock().getType() == Material.IRON_BLOCK) {
				Location loc = event.getPlayer().getLocation();
				this.plugin.getLogger().info(loc.toString());
				loc.add(0, -1, 0);
				if (event.getClickedBlock().getLocation().distanceSquared(loc) < 1.0) {

					int airCount = 0;
					int relativeAltitude = 1;
					boolean pocketFound = false;
					boolean goldFound = false;
					if (event.getAction().name() == "RIGHT_CLICK_BLOCK") {
						while (!pocketFound && relativeAltitude + event.getClickedBlock().getY() < 259) {
							relativeAltitude++;
							if (event.getClickedBlock().getRelative(0, relativeAltitude, 0).getType() == Material.AIR) {
								airCount++;
								if (goldFound && airCount > 1) {
									loc.setY(event.getClickedBlock().getY() + relativeAltitude - 1);
									pocketFound = true;
								}
							} else {
								airCount = 0;
								goldFound = false;
								if (event.getClickedBlock().getRelative(0, relativeAltitude, 0)
										.getType() == Material.IRON_BLOCK) {
									goldFound = true;
								}
							}
						}
						if (relativeAltitude + event.getClickedBlock().getY() > 259) {
							return;
						}
					} else { // LEFT CLICK
						while (!pocketFound && relativeAltitude + event.getClickedBlock().getY() > 0) {
							relativeAltitude--;
							if (event.getClickedBlock().getRelative(0, relativeAltitude, 0).getType() == Material.AIR) {
								airCount++;
							} else {
								goldFound = false;
								if (event.getClickedBlock().getRelative(0, relativeAltitude, 0)
										.getType() == Material.IRON_BLOCK) {
									goldFound = true;
								}
								if (goldFound && airCount > 1) {
									loc.setY(event.getClickedBlock().getY() + relativeAltitude + 1);
									pocketFound = true;
								}
								airCount = 0;
							}
						}
						if (relativeAltitude + event.getClickedBlock().getY() < 1) {
							return;
						}
					}
					event.getPlayer().teleport(loc);
					this.plugin.getLogger().info(loc.toString());
					event.getPlayer().setVelocity(new Vector(0, 0, 0));
				}
			}
		}
	}

	@EventHandler
	public void bb(BlockBreakEvent event) {
		if (this.plugin.stoneList.containsKey(event.getBlock().getType())) {
			if (blockReinforce.isReinforced(event.getBlock())) {
				return;
			}
			for (ProtectedRegion region : getSet(event.getPlayer().getLocation())) {
				int tx = region.getFlag(GuardStones.SOURCEX);
				if (tx == event.getBlock().getX()) {
					int ty = region.getFlag(GuardStones.SOURCEY);
					if (ty == event.getBlock().getY()) {
						int tz = region.getFlag(GuardStones.SOURCEZ);
						if (tz == event.getBlock().getZ()) {
							container.get(event.getPlayer().getWorld()).removeRegion(region.getId());
							event.getPlayer().sendMessage("Field Destroyed");
						}
					}
				}
			}
		}
	}



	@EventHandler
	public void onSquat(PlayerToggleSneakEvent event) {
		if (event.isSneaking()) {

//			LocalPlayer p = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
			for (ProtectedRegion region : getSet(event.getPlayer().getLocation())) {
				int lif = region.getFlag(GuardStones.LIFE);
				if (!isMember(event.getPlayer(), region)) {
					if (readyYet(event.getPlayer())) {
						
						if (((System.currentTimeMillis() / 3600000) < (lif - 1))) {
							region.setFlag(GuardStones.LIFE, lif - 1);
							event.getPlayer().sendMessage(
									"life left = " + ((lif - 1) - (System.currentTimeMillis() / 3600000)));
						} else {
							container.get(event.getPlayer().getWorld()).removeRegion(region.getId());
							event.getPlayer().sendMessage("Field Destroyed");
						}
					}
				} else {
					event.getPlayer().sendMessage("life left = " + ((lif) - (System.currentTimeMillis() / 3600000)));
				}
			}
		}
	}

	public ApplicableRegionSet getSet(Location l) {
		RegionQuery query = container.createQuery();
		return query.getApplicableRegions(l);

	}

	public boolean isMember(Player p, ProtectedRegion r) {
		String nlg = r.getFlag(GuardStones.NLGROUP);
		Group g = vg.civcraft.mc.namelayer.GroupManager.getGroup(nlg);
		if (g.isMember(p.getUniqueId())) {
			return true;
		}
		return false;
	}

	public boolean readyYet(Player player) {

		if (placePlayer.containsKey(player)) {
			if (placePlayer.get(player) > System.currentTimeMillis()) {
				player.sendMessage("not yet " + (placePlayer.get(player) - System.currentTimeMillis()));
				return false;
			}
		}
		placePlayer.put(player, (System.currentTimeMillis() + 60000));
		return true;

	}
}
