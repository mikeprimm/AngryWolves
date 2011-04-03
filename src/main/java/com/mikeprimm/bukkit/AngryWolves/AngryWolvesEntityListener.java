
package com.mikeprimm.bukkit.AngryWolves;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.List;

/**
 * Entity listener - listen for spawns of wolves
 * @author MikePrimm
 */
public class AngryWolvesEntityListener extends EntityListener {
    private final AngryWolves plugin;
    private final Random rnd = new Random(System.currentTimeMillis());
    
    public AngryWolvesEntityListener(final AngryWolves plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if(event.getCreatureType().equals(CreatureType.WOLF)) {
    		Location loc = event.getLocation();
    		System.out.println("A Wolf Spawned at " + loc);
    		if((rnd.nextInt() % 100) > plugin.getRateByWorld(loc.getWorld())) {
    			Wolf w = (Wolf)event.getEntity();
    			if(w.isAngry() == false) {
    				w.setAngry(true);
    				/* And get our spawn message */
    				String sm = plugin.getSpawnMsgByWorld(loc.getWorld());
    				if(sm != null) {
    					List<Player> pl = loc.getWorld().getPlayers();
    					for(Player p : pl) {
    						p.sendMessage(sm);
    					}
    				}
    			}
    		}
    	}
    }       
}
