
package com.mikeprimm.bukkit.AngryWolves;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
/**
 * Entity listener - listen for spawns of wolves
 * @author MikePrimm
 */
public class AngryWolvesEntityListener implements Listener {
    private final AngryWolves plugin;
    private final Random rnd = new Random(System.currentTimeMillis());
    private Map<String, Long> msg_ts_by_world = new HashMap<String, Long>();
    private static final long SPAM_TIMER = 60000;
    private static Map<Integer, HellHoundRecord> hellhound_ids = new HashMap<Integer, HellHoundRecord>();
    private static Set<Integer> angrywolf_ids = new HashSet<Integer>();
    private static final int HELLHOUND_FIRETICKS = 60*20;	/* Do 60 seconds at a time */
    
    private static class HellHoundRecord {
        int fireball_tick;
    }
    
    public AngryWolvesEntityListener(final AngryWolves plugin) {
        this.plugin = plugin;
    }

    private static boolean doing_spawn;
    
    private static class DoSpawn implements Runnable {
    	Location loc;
    	Player tgt;
    	boolean is_hellhound;
    	boolean angry;
    	boolean huntvillagers;
    	boolean pup;
    	int health;
    	public void run() {
    	    doing_spawn = true;
    		Wolf w = (Wolf)loc.getWorld().spawn(loc, Wolf.class);
    		doing_spawn = false;
    		if(w != null) {
    		    if(angry)
    		        AngryWolves.setAngry(w, true, huntvillagers);
    			if(tgt != null)
    				w.setTarget(tgt);
    			if(is_hellhound) {
    				hellhound_ids.put(Integer.valueOf(w.getEntityId()), new HellHoundRecord());	/* Add to table */
    				w.setFireTicks(HELLHOUND_FIRETICKS);	/* Set it on fire */
    			}
    			if(angry) {
    			    w.setMaxHealth(health);
                    w.setHealth(health);
    			    angrywolf_ids.add(Integer.valueOf(w.getEntityId()));
    			}
    			if(pup) {
    			    w.setAge(-24000);
    			}
    		}
    	}
    }
    private boolean checkLimit() {
    	return angrywolf_ids.size() < plugin.getPopulationLimit();
    }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if(event.isCancelled())
    		return;
    	if(doing_spawn)
    	    return;
    	Location loc = event.getLocation();
    	boolean did_it = false;
    	Entity ent = event.getEntity();
    	if(ent == null)
    		return;
    	AngryWolves.BaseConfig cfg = null;
    	/* If monster spawn */
    	if((ent instanceof Zombie) || (ent instanceof Creeper) ||
    		(ent instanceof Spider) || (ent instanceof Skeleton) ||
    		(ent instanceof PigZombie)) {
    		/* Find configuration for our location */
    		cfg = plugin.findByLocation(loc);
    		//plugin.log.info("mob: " + cfg);
    		int rate = cfg.getMobToWolfRate(ent, plugin.isFullMoon(loc.getWorld()));
    		int rate2 = cfg.mobToWildWolfRate();
    		if(plugin.verbose) AngryWolves.log.info("mobrate(" + ent.getClass().getName() + ")=" + rate);
    		/* If so, percentage is relative to population of monsters (percent * 10% is chance we grab */
    		int rndval = rnd.nextInt(1000);
    		if(((rate+rate2) > 0)  && (rndval < (rate+rate2)) && checkLimit()) {
    		    boolean angry = (rndval < rate);
    			boolean ignore_terrain = cfg.getMobToWolfTerrainIgnore();	/* See if we're ignoring terrain */
        		Block b = loc.getBlock();
        		Biome bio = b.getBiome();
        		/* See if hellhound - only hellhounds substitute in Nether, use hellhound_rate elsewhere */
        		boolean do_hellhound = angry && ((bio.equals(Biome.NETHER) || (rnd.nextInt(100) <= cfg.getHellhoundRate())));

                if(plugin.verbose) AngryWolves.log.info("biome=" + bio + ", ignore=" + ignore_terrain + ", angry=" + angry + ", hellhound=" + do_hellhound);
        		/* If valid biome for wolf (or hellhound) */
        		if(ignore_terrain || (bio == Biome.FOREST) || (bio == Biome.TAIGA) ||
        				(bio == Biome.NETHER) || (bio == Biome.WOODED_HILLS) || (bio == Biome.TAIGA_HILLS)) {
    				/* If hellhound in hell, we're good */
    				if(bio.equals(Biome.NETHER)) {
    				    if(!do_hellhound)   /* Only angry hellhounds in nether */
    				        return;
    				}
    				else if(!ignore_terrain) {
        				while((b != null) && (b.getType().equals(Material.AIR))) {
        					b = b.getRelative(BlockFace.DOWN, 1);
        				}
        				/* Quit if we're not over soil */
        				if((b == null) || (!b.getType().equals(Material.GRASS))) {
        					if(plugin.verbose) AngryWolves.log.info("material=" + b.getType()); 
        					return;
        				}
        			}
    				event.setCancelled(true);
    				DoSpawn ds = new DoSpawn();
    				ds.loc = loc;
    				ds.is_hellhound = do_hellhound;
    				ds.angry = angry;
    				ds.health = do_hellhound?plugin.getHellhoundHealth():plugin.getAngryWolfHealth();
    				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ds);
    				did_it = true;
    				if(plugin.verbose) AngryWolves.log.info("Mapped " + ent.getClass().getName() + " spawn to " + (angry?(do_hellhound?"hellhound":"angry world"):"wolf"));
    			}
    		}
    	}
    	else if(ent instanceof Wolf) {
    		Wolf w = (Wolf)event.getEntity();
    		/* If not angry and not tame  */
    		if((w.isAngry() == false) && (plugin.isTame(w) == false) && (!plugin.isNormalSpawn())) {
        		cfg = plugin.findByLocation(loc);
        		//plugin.log.info("wolf: " + cfg);
    			int rate = cfg.getSpawnAngerRate();
    			int fmrate = cfg.getSpawnAngerRateMoon();
    			/* If higher rate during full moon, check if we're having one */
    			if((fmrate > rate) && plugin.isFullMoon(loc.getWorld())) {	
    				rate = fmrate;
    			}
    			if((rate > 0) && (rnd.nextInt(100) <= rate) && checkLimit()) {
    				AngryWolves.setAngry(w, true, cfg.huntVillagers());
    				/* See if it is a hellhound! */
    				rate = cfg.getHellhoundRate();
    				if((rate > 0) && (rnd.nextInt(100) <= rate)) {
    					hellhound_ids.put(w.getEntityId(), new HellHoundRecord());
    					w.setFireTicks(HELLHOUND_FIRETICKS);
        				if(plugin.verbose) AngryWolves.log.info("Made a spawned wolf into a hellhound");
        				int hlth = plugin.getHellhoundHealth();
        				w.setMaxHealth(hlth);
        				w.setHealth(hlth);
    				}
    				else {
                        int hlth = plugin.getAngryWolfHealth();
                        w.setMaxHealth(hlth);
    					w.setHealth(hlth);
    					if(plugin.verbose) AngryWolves.log.info("Made a spawned wolf angry");
    				}
    				did_it = true;
    			}
    		}
    	}
    	if(did_it) {
    		/* And get our spawn message */
    		String sm = cfg.getSpawnMsg();
    		double radius = (double)cfg.getSpawnMsgRadius();
			if((sm != null) && (sm.length() > 0)) {
				/* See if too soon (avoid spamming these messages) */
				Long last = msg_ts_by_world.get(loc.getWorld().getName());
				if((last == null) || ((last.longValue() + SPAM_TIMER) < System.currentTimeMillis())) {
					msg_ts_by_world.put(loc.getWorld().getName(), Long.valueOf(System.currentTimeMillis()));
					List<Player> pl = loc.getWorld().getPlayers();
					for(Player p : pl) {
						if(radius > 0.0) {	/* If radius limit, see if player is close enough */
							Location ploc = p.getLocation();
							double dx = ploc.getX() - loc.getX();
							double dy = ploc.getY() - loc.getY();
							double dz = ploc.getZ() - loc.getZ();
							if(((dx*dx) + (dy*dy) + (dz*dz)) <= (radius*radius)) {
								p.sendMessage(AngryWolves.processMessage(sm));
							}
						}
						else
							p.sendMessage(AngryWolves.processMessage(sm));
					}
				}
  			}
			AngryWolves.playHowl(loc);
            if(plugin.verbose) AngryWolves.log.info("Make howl sound");
    	}
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
    	if(event.isCancelled())
    		return;
		Entity e = event.getEntity();
    	/* If fire damage, see if it is a hellhound */
        HellHoundRecord r = hellhound_ids.get(e.getEntityId());
    	if(r != null) {
    	    Wolf w = (Wolf)e;
    		e.setFireTicks(HELLHOUND_FIRETICKS);
            DamageCause dc = event.getCause();
            LivingEntity tgt = w.getTarget();
            if((dc == DamageCause.FIRE_TICK) && (tgt != null)) { 
    		    Location loc = w.getEyeLocation();
	            AngryWolves.BaseConfig cfg = plugin.findByLocation(loc);
	            int rate = cfg.getHellhoundFireballRate();
	            if(rate > 0) {
	                if(r.fireball_tick < rate)
	                    r.fireball_tick++;
	                if(r.fireball_tick >= rate) {
	                    Location tloc = tgt.getLocation();
	                    double dist2 = tloc.distanceSquared(loc);
	                    int range = cfg.getHellhoundFireballRange();
	                    if((dist2 >= 4.0) && (dist2 <= range*range)) {  /* Don't do fireballs when close in, or too far away */
                            Vector dir = tloc.subtract(loc).toVector();    /* Get direction to target */
                            Vector start = dir.multiply(1.0/dir.length());
                            Fireball fireball = loc.getWorld().spawn(loc.add(start), Fireball.class);
                            if(fireball != null) {
                                fireball.setDirection(dir);
                                fireball.setShooter(w);
                                fireball.setIsIncendiary(cfg.getHellhoundFireballIncendiary());
                            }
                            r.fireball_tick = 0;
	                    }
	                }
	            }
    		}
        	if((dc == DamageCause.FIRE_TICK) || (dc == DamageCause.FIRE) || (dc == DamageCause.LAVA) || 
        	        ((dc == DamageCause.ENTITY_EXPLOSION) && (event.getEntity() instanceof Fireball))) {
    			event.setCancelled(true);	/* Cancel it - we're fireproof! */
    			return;
    		}
        	else if (plugin.getHellhoundDamageScale() != 1.0) {
    			int dmg = (int)Math.round(plugin.getHellhoundDamageScale() * (double)event.getDamage());
    			if(dmg != event.getDamage()) {
    				event.setDamage(dmg);
    			}
        	}
    	}
    	if(!(event instanceof EntityDamageByEntityEvent))
    		return;
    	EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
    	Entity damager = evt.getDamager();
    	if(damager instanceof Player) {
    		Player p = (Player)damager;
    		/* See if its a sheep */
    		if(!(e instanceof Sheep))
    			return;
    		Sheep s = (Sheep)e;
    		Location loc = s.getLocation();
    		AngryWolves.BaseConfig cfg = plugin.findByLocation(loc);
    		//plugin.log.info("sheep: " + cfg);
    		int rate = cfg.getWolfInSheepRate();
    	
    		/* Use hashcode - random enough, and makes it so that something damaged
    		 * once will trigger, or never will, even if damaged again */
    		if(new Random(e.hashCode()).nextInt(1000) >= rate)
    			return;
    		String msg = cfg.getWolfInSheepMsg();
    		if((msg != null) && (msg.length() > 0))
    			p.sendMessage(AngryWolves.processMessage(cfg.getWolfInSheepMsg()));
    		evt.setCancelled(true);	/* Cancel event */
    		e.remove();	/* Remove the sheep */
    	
    		DoSpawn ds = new DoSpawn();
    		ds.loc = loc;
    		ds.tgt = p;
    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ds);
    		if(plugin.verbose) AngryWolves.log.info("Made attacked sheep into angry wolf"); 
    	}
    	else if(damager instanceof Wolf) {
    		if(!(e instanceof Player)) {	/* Not a player - don't worry */
    			return;
    		}
    		/* If we don't do wolf-friends here, skip it (check based on player's location) */
    		AngryWolves.BaseConfig cfg = plugin.findByLocation(e.getLocation());
    		if(plugin.verbose) AngryWolves.log.info("wolffriend: " + cfg.getWolfFriendActive());
    		if(cfg.getWolfFriendActive() == false) {
    			return;
    		}
    		if(AngryWolvesPermissions.permission((Player)e, AngryWolves.WOLF_FRIEND_PERM)) {
    			event.setCancelled(true);	/* Cancel it */
    			((Wolf)damager).setTarget(null);	/* Target someone else */
    			if(plugin.verbose) AngryWolves.log.info("Cancelled attack on wolf-friend"); 
    		}
    	}
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
    	if(event.isCancelled())
    		return;
    	Entity e = event.getEntity();
    	if(!(e instanceof Wolf))	/* Don't care about non-wolves */
    		return;
    	if(hellhound_ids.containsKey(e.getEntityId()))
    		e.setFireTicks(HELLHOUND_FIRETICKS);
    	Entity t = event.getTarget();
    	if(!(t instanceof Player)) 	/* Don't worry about non-player targets */
    		return;
    	if(plugin.verbose) AngryWolves.log.info("Wolf considering player as target"); 
    	Player p = (Player)t;
    	/* If we don't do wolf-friends here, skip it (check based on player's location) */
    	AngryWolves.BaseConfig cfg = plugin.findByLocation(p.getLocation());
		if(cfg.getWolfFriendActive() == false) {
			return;
		}
    	if(AngryWolvesPermissions.permission(p, AngryWolves.WOLF_FRIEND_PERM)) {	/* If player is a wolf-friend */
    		event.setCancelled(true);	/* Cancel it - not a valid target */
    		if(plugin.verbose) AngryWolves.log.info("Cancelled target on wolf friend");
    	}
    }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        if(!(e instanceof Wolf)) {    /* Don't care about non-wolves */
            if(e instanceof Sheep) {    /* Sheep killed?  See if wolf did it */
                AngryWolves.BaseConfig cfg = plugin.findByLocation(e.getLocation());    /* Get our configuration for location */
                int rate = cfg.getPupOnSheepKillRate();
                if(rate > 0) {
                    EntityDamageEvent ede = e.getLastDamageCause();
                    if(ede instanceof EntityDamageByEntityEvent) {
                        EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent)ede;
                        Entity damager = edee.getDamager();
                        if(damager instanceof Wolf) {   /* Killed by a wolf? */
                            if((rate < rnd.nextInt(100)) && checkLimit()) {
                                if(plugin.verbose) AngryWolves.log.info("Spawning pup");
                                DoSpawn ds = new DoSpawn();
                                ds.loc = e.getLocation();
                                ds.pup = true;
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ds, 40);
                            }
                        }
                    }
                }
            }
            return;
        }
        Wolf w = (Wolf)e;
        /* Forget the dead hellhound */
        boolean was_hellhound = hellhound_ids.remove(e.getEntityId()) != null;
        angrywolf_ids.remove(e.getEntityId());
        /* Check for loot */
        AngryWolves.BaseConfig cfg = plugin.findByLocation(e.getLocation());    /* Get our configuration for location */
        boolean drop_loot = false;
        boolean drop_xp = false;
        EntityDamageEvent ede = w.getLastDamageCause();
        if((ede != null) && ((ede.getCause() == DamageCause.ENTITY_ATTACK) || (ede.getCause() == DamageCause.PROJECTILE))) {
            drop_xp = true;
        }
        if(was_hellhound && (cfg.getHellHoundLootRate() >= 0)) {
        	drop_loot = cfg.getHellHoundLootRate() > rnd.nextInt(100);
        	if(drop_xp)
        	    event.setDroppedExp(cfg.getHellHoundXP());
        }
        else if(w.isAngry() && (cfg.getAngryWolfLootRate() >= 0)) {
        	drop_loot = cfg.getAngryWolfLootRate() > rnd.nextInt(100);
            if(drop_xp)
                event.setDroppedExp(cfg.getAngryWolfXP());
        }
        else {
        	drop_loot = cfg.getWolfLootRate() > rnd.nextInt(100);        	
            if(drop_xp)
                event.setDroppedExp(cfg.getWolfXP());
        }
        if(drop_loot) {
            List<Integer> loot;
            if(was_hellhound && (cfg.getHellHoundLoot() != null))
            	loot = cfg.getHellHoundLoot();
            else if(w.isAngry() && (cfg.getAngryWolfLoot() != null))
            	loot = cfg.getAngryWolfLoot();
            else
            	loot = cfg.getWolfLoot();
            int sz = loot.size();
            if(sz > 0) {
                int id = loot.get(rnd.nextInt(sz));
                List<ItemStack> drop = event.getDrops();
                if(drop != null) {
                    drop.add(new ItemStack(Material.GOLDEN_APPLE , 1));
                }
            }
        }
    }    
    
    public static final boolean isHellHound(Entity e) {
    	return hellhound_ids.containsKey(e.getEntityId());
    }
}
