package com.mikeprimm.bukkit.AngryWolves;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Permissions and GroupManager handling - inspired by BigBrother code
 * @author mike
 *
 */
public class AngryWolvesPermissions {
	private enum Handler {
		NONE,
		PERMISSIONS
	};
	private static Handler our_handler;
	private static Permissions permissions_plugin;
	private static PermissionHandler handler;
	
	public static void initialize(Server server) {
		Plugin perm = server.getPluginManager().getPlugin("Permissions");
		if(perm != null) {
		    try {
		        permissions_plugin = (Permissions)perm;
		        handler = permissions_plugin.getHandler();
                our_handler = Handler.PERMISSIONS;
		        AngryWolves.log.info("[AngryWolves] Using Permissions " + permissions_plugin.getDescription().getVersion() + " for access control");
		    } catch (NoClassDefFoundError ncdf) {
		    }
		}
		if(our_handler == Handler.NONE) {
            AngryWolves.log.info("[AngryWolves] Using Bukkit API for access control");
		}
	}
	/* Fetch specific permission for given player */
	public static boolean permission(Player player, String perm) {
		switch(our_handler) {
			case PERMISSIONS:
				return handler.has(player, perm);
			case NONE:
			default:
				return player.hasPermission(perm);
		}
	}
}
