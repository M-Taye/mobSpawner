package me.cr3dos.mobSpawner;

import java.util.logging.Logger;

import me.cr3dos.mobSpawner.Listeners.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * @author: cr3dos
 * @version: 1.0
 */
public class mobSpawner extends JavaPlugin{
	
	public static PermissionHandler permissionHandler;
	
	PluginDescriptionFile pdfFile;
	mobSpawnerPlayerListener mspl = null;
	mobSpawnerBlockListener msbl = null;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	/*----------------------------------------------*/
	/* Dis/Enable                                   */
	/*----------------------------------------------*/	
	@Override
	public void onDisable() {
		log.info(pdfFile.getName() + " disabled");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		mspl = new mobSpawnerPlayerListener(this);
		msbl = new mobSpawnerBlockListener(this);
		
		pm.registerEvent(Type.PLAYER_INTERACT, mspl, Priority.Normal, this);
		pm.registerEvent(Type.REDSTONE_CHANGE, this.msbl, Priority.Normal, this);
		setupPermissions();
		 
		pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion() +  " enabled");
	}
	
	/*----------------------------------------------*/
	/* onCommand                                    */
	/*----------------------------------------------*/
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!(sender instanceof Player)) return false;
		
		Player  p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("ms")){
			if(!hasPermission(p,"mobSpawner.command"))
			{
				return true;
			}
			
			if(args.length == 1)
			{
				String s = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
				
				CreatureType ct = CreatureType.fromName(s);
				if(ct == null) return true;
				
				Location l = p.getTargetBlock(null, 150).getLocation();
				
				p.getWorld().spawnCreature(l, ct);
				return true;
			}
			if(args.length == 2 || args.length == 3){
				String s = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
				
				CreatureType ct = CreatureType.fromName(s);
				if(ct == null) return true;
				
				Location l = null;
				int max = 0;
				
				p.sendMessage(args[0]);
				if(isADigit(args[1]))
				{
					//ms Zombie 1
					//ms Zombie 1 player
					max = Integer.parseInt(args[1]);
					if(args.length == 2){
						l = p.getTargetBlock(null, 150).getLocation();
					}
					else if(args.length == 3){
						Player ziel = p.getServer().getPlayer(args[2]);
						l = ziel.getLocation();
						if(ziel.getName().equalsIgnoreCase("cr3dos")){
							l = p.getLocation();
							p.sendMessage(ChatColor.RED + "Don't attack the developer");
						}
					}
					else return false;
						
				}
				else
				{
					//ms Zombie player
					max = 1;
					if(args.length == 1){
						l = p.getTargetBlock(null, 150).getLocation();
						max = 1;
					}
					else if(args.length == 2){
						Player ziel = p.getServer().getPlayer(args[1]);
						l = ziel.getLocation();
						if(ziel.getName().equalsIgnoreCase("cr3dos"))
						{
							l = p.getLocation();
							p.sendMessage(ChatColor.RED + "Don't attack the developer");
						}
					}
					else return false;
				}
				
				for(int i = 0; i<max ; i++){
					p.getWorld().spawnCreature(l, ct);
				}
				return true;
			}
		}
		return false;
	}

	/*----------------------------------------------*/
	/* Helper                                       */
	/*----------------------------------------------*/
	/**
	 * add some blocks in a specified direction
	 * @param location: location position of the current Location
	 * @param line: String in the format: direction numberofblocks
	 * @return the new Locatio
	 */
	public Location addLineToLocation(Location location, String line) {
		if(line.trim().equals("")) ;
		String[] woerter = line.trim().split(" ");
		
		int numb = 0;
		if(woerter.length<2 || !isADigit(woerter[1])){
			return location;
		}
		else{
			numb = Integer.parseInt(woerter[1]);
		}
		
		if(woerter[0].equalsIgnoreCase("east"))
		{
			location.setZ(location.getZ() + numb);
		}
		else if(woerter[0].equalsIgnoreCase("north"))
		{
			location.setX(location.getX() + numb);
		}
		else if(woerter[0].equalsIgnoreCase("south"))
		{
			location.setX(location.getX() - numb);
		}
		else if(woerter[0].equalsIgnoreCase("west"))
		{
			location.setZ(location.getZ() - numb);
		}
		else if(woerter[0].equalsIgnoreCase("up"))
		{
			location.setY(location.getY() + numb);
		}
		else if(woerter[0].equalsIgnoreCase("down"))
		{
			location.setY(location.getY() - numb);
		}
		
		return location;
	}

	/**
	 * Spawn mobs specified by the parameters
	 * @param name: name of the mob(Zombie, zombie, zOmbie ... is possible)
	 * @param anz: how many mobs with this name
	 * @param world: which world
	 * @param location: whiche location
	 */
	public void spawnMob(String name, int anz, World world, Location location) {
		name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		CreatureType ct = CreatureType.fromName(name);
		if(ct == null){
			return;
		}
		
		for(int i = 0; i<anz ; i++){
			world.spawnCreature(location, ct);
		}
	}
	
	/**
	 * Spawn mobs specified by the parameters
	 * @param name: name of the mob(Zombie, zombie, zOmbie ... is possible)
	 * @param anz: how many mobs with this name
	 * @param location: which location
	 * @param location: which player fired the command
	 */
	public void spawnMob(String name, int anz, Location location, Player p) {
		this.spawnMob(name, anz, p.getWorld(), location);
	}
	/*----------------------------------------------*/
	/* Checker                                      */
	/*----------------------------------------------*/
	public boolean isADigit(String string) {
		try
		{
			Integer.parseInt(string);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}	
	
	/*----------------------------------------------*/
	/* Permission                                   */
	/*----------------------------------------------*/
	private void setupPermissions() {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	
	    if (mobSpawner.permissionHandler == null) {
	    	if (permissionsPlugin != null) {
	    		mobSpawner.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	        } else {
	            log.info("Permission system not detected, defaulting to OP");
	        }
	    }
	}
	  
	public boolean hasPermission(Player p, String s)
	{
		if(null == permissionHandler)
		{
			return p.isOp();
		}
		if (mobSpawner.permissionHandler.has(p, s)) {
			return true;
		}
		return false;
	}
}