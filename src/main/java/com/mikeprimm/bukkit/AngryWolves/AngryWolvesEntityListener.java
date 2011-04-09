
package com.mikeprimm.bukkit.AngryWolves;

import org.bukkit.entity.CreatureType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity listener - listen for spawns of wolves
 * @author MikePrimm
 */
public class AngryWolvesEntityListener extends EntityListener {
    private final AngryWolves plugin;
    private final Random rnd = new Random(System.currentTimeMillis());
    private Map<String, Long> msg_ts_by_world = new HashMap<String, Long>();
    private static final long SPAM_TIMER = 60000;
    
    public AngryWolvesEntityListener(final AngryWolves plugin) {
        this.plugin = plugin;
    }

    private static class DoSpawn implements Runnable {
    	Location loc;
    	Player tgt;
    	public void run() {
    		Wolf w = (Wolf)loc.getWorld().spawnCreature(loc, CreatureType.WOLF);
    		if(w != null) {
    			w.setAngry(true);
    			if(tgt != null)
    				w.setTarget(tgt);
    		}
    	}
    }
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if(event.isCancelled())
    		return;
    	Location loc = event.getLocation();
    	boolean did_it = false;
    	CreatureType ct = event.getCreatureType();
    	/* If monster spawn */
    	if(ct.equals(CreatureType.ZOMBIE) || ct.equals(CreatureType.CREEPER) ||
    		ct.equals(CreatureType.SPIDER) || ct.equals(CreatureType.SKELETON)) {
    		int rate = plugin.getMobToWolfRateByWorld(loc.getWorld());
    		/* If so, percentage is relative to population of monsters (percent * 10% is chance we grab */
    		if((rate > 0) && (rnd.nextInt(1000) < rate)) {
        		Block b = loc.getBlock();
        		Biome bio = b.getBiome();
        		/* If valid biome for wolf */
        		if(bio.equals(Biome.FOREST) || bio.equals(Biome.TAIGA) || bio.equals(Biome.SEASONAL_FOREST)) {
        			while((b != null) && (b.getType().equals(Material.AIR))) {
        				b = b.getFace(BlockFace.DOWN);
        			}
        			/* Quit if we're not over soil */
        			if((b == null) || (!b.getType().equals(Material.GRASS))) {
        				return;
        			}

    				event.setCancelled(true);
    				DoSpawn ds = new DoSpawn();
    				ds.loc = loc;
    				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ds);
    				did_it = true;
    			}
    		}
    	}
    	else if(ct.equals(CreatureType.WOLF)) {
    		Wolf w = (Wolf)event.getEntity();
    		/* If not angry and not tame  */
    		if((w.isAngry() == false) && (plugin.isTame(w) == false) && (!plugin.isNormalSpawn())) {
    			int rate = plugin.getSpawnRateByWorld(loc.getWorld());
    			if((rate > 0) && (rnd.nextInt(100) < rate)) {
    				w.setAngry(true);
    				did_it = true;
    			}
    		}
    	}
    	if(did_it) {
    		/* And get our spawn message */
    		String sm = plugin.getSpawnMsgByWorld(loc.getWorld());
			if(sm != null) {
				/* See if too soon (avoid spamming these messages) */
				Long last = msg_ts_by_world.get(loc.getWorld().getName());
				if((last == null) || ((last.longValue() + SPAM_TIMER) < System.currentTimeMillis())) {
					msg_ts_by_world.put(loc.getWorld().getName(), Long.valueOf(System.currentTimeMillis()));
					List<Player> pl = loc.getWorld().getPlayers();
					for(Player p : pl) {
						p.sendMessage(sm);
					}
				}
  			}
    	}
    }
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
    	if(event.isCancelled())
    		return;
    	if(!(event instanceof EntityDamageByEntityEvent))
    		return;
    	EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
    	Entity damager = evt.getDamager();
    	if(damager instanceof Player) {
    		Player p = (Player)damager;
    		/* See if its a sheep */
    		Entity e = evt.getEntity();
    		if(!(e instanceof Sheep))
    			return;
    		Sheep s = (Sheep)e;
    		Location loc = s.getLocation();
    		int rate = plugin.getWolfInSheepRateByWorld(loc.getWorld());
    	
    		/* Use hashcode - random enough, and makes it so that something damaged
    		 * once will trigger, or never will, even if damaged again */
    		if(new Random(e.hashCode()).nextInt(1000) >= rate)
    			return;
    		p.sendMessage(plugin.getWolfInSheepMsgByWorld(loc.getWorld()));
    		evt.setCancelled(true);	/* Cancel event */
    		e.remove();	/* Remove the sheep */
    	
    		DoSpawn ds = new DoSpawn();
    		ds.loc = loc;
    		ds.tgt = p;
    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ds);
    	}
    	else if(damager instanceof Wolf) {
    		Entity e = event.getEntity();
    		if(!(e instanceof Player)) {	/* Not a player - don't worry */
    			return;
    		}
    		/* If we don't do wolf-friends here, skip it */
    		if(plugin.getWolfFriendsByWorld(e.getWorld()) == false) {
    			return;
    		}
    		if(AngryWolvesPermissions.permission((Player)e, AngryWolves.WOLF_FRIEND_PERM)) {
    			event.setCancelled(true);	/* Cancel it */
    			((Wolf)damager).setTarget(null);	/* Target someone else */
    		}
    	}
    }
    @Override
    public void onEntityTarget(EntityTargetEvent event) {
    	if(event.isCancelled())
    		return;
    	Entity e = event.getEntity();
    	if(!(e instanceof Wolf))	/* Don't care about non-wolves */
    		return;
    	Entity t = event.getTarget();
    	if(!(t instanceof Player)) 	/* Don't worry about non-player targets */
    		return;
    	Player p = (Player)t;
    	/* If we don't do wolf-friends here, skip it */
		if(plugin.getWolfFriendsByWorld(p.getWorld()) == false) {
			return;
		}
    	if(AngryWolvesPermissions.permission(p, AngryWolves.WOLF_FRIEND_PERM)) {	/* If player is a wolf-friend */
    		event.setCancelled(true);	/* Cancel it - not a valid target */
    	}
    }
}
