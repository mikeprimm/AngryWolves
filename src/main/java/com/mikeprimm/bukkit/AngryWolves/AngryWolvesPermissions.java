package com.mikeprimm.bukkit.AngryWolves;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.anjocaido.groupmanager.GroupManager;

/**
 * Permissions and GroupManager handling - inspired by BigBrother code
 * @author mike
 *
 */
public class AngryWolvesPermissions {
	private enum Handler {
		NONE,
		PERMISSIONS,
		GROUP_MANAGER
	};
	private static Handler our_handler;
	private static Plugin permissions_plugin;
	private static Object handler;
	
	public static void initialize(Server server) {
		Plugin perm = server.getPluginManager().getPlugin("GroupManager");
		if(perm != null) {
			our_handler = Handler.GROUP_MANAGER;
			permissions_plugin = perm;
		}
		else {
			perm = server.getPluginManager().getPlugin("Permissions");
			if(perm != null) {
				our_handler = Handler.PERMISSIONS;
				permissions_plugin = perm;
				handler = ((Permissions)permissions_plugin).getHandler();
			}
			else {
				our_handler = Handler.NONE;
				permissions_plugin = null;
			}
		}
	}
	/* Fetch specific permission for given player */
	public static boolean permission(Player player, String perm) {
		switch(our_handler) {
			case PERMISSIONS:
				return ((PermissionHandler)handler).has(player, perm);
			case GROUP_MANAGER:
				return ((GroupManager)permissions_plugin).getWorldsHolder().getWorldPermissions(player).has(player, perm);
			case NONE:
			default:
				return player.isOp();
		}
	}
}
