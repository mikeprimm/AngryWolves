
package com.mikeprimm.bukkit.AngryWolves;
import java.util.HashMap;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkit.Location;
import org.bukkit.World;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import org.bukkit.craftbukkit.entity.CraftWolf;

/**
 * AngryWolves plugin - watch wolf spawns and make some of them angry by default
 *
 * @author MikePrimm
 */
public class AngryWolves extends JavaPlugin {
    private final AngryWolvesEntityListener entityListener = new AngryWolvesEntityListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

    /* Deprecated parms */
    public static final String CONFIG_ANGERRATE_DEFAULT = "angerrate";
    public static final String CONFIG_SPAWNMSG_DEFAULT = "spawnmsg";
    public static final String CONFIG_ASALTMOB_DEFAULT = "asaltmob";
    /* New parameters */
    public static final String CONFIG_SPAWN_ANGERRATE = "spawn-anger-rate";
    public static final String CONFIG_MOBTOWOLF_RATE = "mob-to-wolf-rate";
    public static final String CONFIG_DAYSPERMOON = "days-between-fullmoons";
    public static final String CONFIG_ANGERRATE_MOON = "anger-rate-fullmoon";
    public static final String CONFIG_FULLMOONMSG = "fullmoonmsg";
    public static final String CONFIG_WOLFINSHEEP_RATE = "wolf-in-sheep-rate";
    public static final String CONFIG_WOLFINSHEEP_MSG = "wolf-in-sheep-msg";
    public static final int SPAWN_ANGERRATE_DEFAULT = 0;
    public static final int MOBTOWOLF_RATE_DEFAULT = 10;
    public static final int DAYSPERMOON_DEFAULT = 28;
    public static final int ANGERRATE_MOON_DEFAULT = 0;
    public static final int WOLFINSHEEP_RATE = 0;
    
    private static class PerWorldState {
    	String spawnmsg;
    	Integer mobtowolf_rate;
    	Integer angerrate;
    	Integer days_per_moon;
    	Integer angerrate_moon;
    	String fullmoonmsg;
    	boolean moon_is_full;
    	int	daycounter;
    	long	last_time;
    	Integer	wolfinsheep_rate;
    	String wolfinsheep_msg;
    };
    
    private HashMap<String, PerWorldState> per_world = new HashMap<String, PerWorldState>();
    
    private int def_angerrate = SPAWN_ANGERRATE_DEFAULT;
    private String def_spawnmsg = null;
    private int def_mobtowolf_rate = MOBTOWOLF_RATE_DEFAULT;

    private int days_per_moon = DAYSPERMOON_DEFAULT;
    private int def_angerrate_moon = ANGERRATE_MOON_DEFAULT;
    private String def_fullmoonmsg = "";
    private int wolfinsheep_rate = WOLFINSHEEP_RATE;
    private String wolfinsheep_msg = "Oh, no!  A wolf in sheep's clothing!";
    
    private boolean block_spawn_anger = false;	/* Used for anger-free spawn */
    
    private Random rnd = new Random(System.currentTimeMillis());
    
    private PerWorldState getState(String w) {
    	PerWorldState pws = per_world.get(w);
    	if(pws == null) {
    		pws = new PerWorldState();
    		per_world.put(w, pws);
    	}
    	return pws;
    }
    
    boolean isNormalSpawn() {
    	return block_spawn_anger;
    }
    
    private class CheckForMoon implements Runnable {
    	public void run() {
    		List<World> w = getServer().getWorlds();
    		for(World world : w) {
    			int dpm = getDaysPerMoonByWorld(world);	/* Get lunar period */
    			if(dpm <= 0) {	/* Disabled? */
    				getState(world.getName()).moon_is_full = false; /* Not us */
    			}
    			else {
    				PerWorldState pws = getState(world.getName());
    				
    				long t = world.getTime();	/* Get time of day */
    				if(t < pws.last_time) {	/* Day ended? */
    					pws.daycounter++;
    				}
    				pws.last_time = t;
    				
    				long dom = pws.daycounter % dpm;	/* Compute day of "month" */
    				if((dom == (dpm-1)) && ((t % 24000) > 12500)) {
    					if(pws.moon_is_full == false) {
    						pws.moon_is_full = true;
    						/* And handle event */
    						String msg = getFullMoonMsgByWorld(world);
    						if((msg != null) && (msg.length() > 0)) {
    							List<Player> pl = world.getPlayers();
    							for(Player p : pl) {
    								p.sendMessage(msg);
    							}
    						}
    						/* And make the wolves angry */
    						int rate = getFullMoonRateByWorld(world);
    						if(rate > 0) { /* If non-zero */
    							List<LivingEntity> lst = world.getLivingEntities();
    							for(LivingEntity le : lst) {
    								if(le instanceof Wolf) {
    									Wolf wolf = (Wolf)le;
    									/* If not angry and not tame, make angry */
    									if((wolf.isAngry() == false) && (isTame(wolf) == false)) {
    										if(rnd.nextInt(100) < rate) {
    											wolf.setAngry(true);
    										}
    									}
    								}
    							}
    						}
    					}
    				}
    				else if(pws.moon_is_full) {	/* Was full, but over now */ 
    					pws.moon_is_full = false;
						/* And make the wolves happy */
						int rate = getFullMoonRateByWorld(world);
						if(rate > 0) { /* If non-zero */
	   						List<LivingEntity> lst = world.getLivingEntities();
							for(LivingEntity le : lst) {
								if(le instanceof Wolf) {
									Wolf wolf = (Wolf)le;
									/* If angry, make not angry */
									if(wolf.isAngry()) {
										wolf.setAngry(false);
										wolf.setTarget(null);
									}
								}
							}
						}
    				}
    			}
    		}
    	}
    }
    
    /* On disable, stop doing our function */
    public void onDisable() {
    	/* Since our registered listeners are disabled by default, we don't need to do anything */
    }

    public void onEnable() {
    	/* Read in our configuration */
        readConfig();

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
        /* Start job to watch for sunset/sunrise (every 30 seconds or so) */
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckForMoon(), 0, 20*30);
    }
    
    private boolean migrateOldSettings(ConfigurationNode cfg) {
    	boolean dirty = false;
    	
    	boolean asmob = cfg.getBoolean(CONFIG_ASALTMOB_DEFAULT, true);
    	Object v = cfg.getProperty(CONFIG_ANGERRATE_DEFAULT);
    	if(v != null) {
    		if(asmob) {	/* Was set to asaltmob=true */
    			cfg.setProperty(CONFIG_SPAWN_ANGERRATE, Integer.valueOf(0));
    			cfg.setProperty(CONFIG_MOBTOWOLF_RATE, v);
    		}
    		else {
    			cfg.setProperty(CONFIG_SPAWN_ANGERRATE, v);
    			cfg.setProperty(CONFIG_MOBTOWOLF_RATE, Integer.valueOf(0));
    		}
    		cfg.removeProperty(CONFIG_ANGERRATE_DEFAULT);
    		cfg.removeProperty(CONFIG_ASALTMOB_DEFAULT);
    		dirty = true;
    	}
    	return dirty;
    }
    
    private void readConfig() {    	
    	File configdir = getDataFolder();	/* Get our data folder */
    	if(configdir.exists() == false) {	/* Not yet defined? */
    		configdir.mkdirs();				/* Create it */
    	}
    	/* Initialize configuration object */
    	File configfile = new File(configdir, "AngryWolves.yml");	/* Our YML file */
    	Configuration cfg = new Configuration(configfile);
    	if(configfile.exists() == false) {	/* Not defined yet? */
    		PrintWriter fos = null;
    		try {
    			fos = new PrintWriter(new FileWriter(configfile));
    			fos.println("# Configuration file for AngryWolves);");
    			fos.println("#   spawn-anger-rate is percentage of normal wolf spawns that spawn angry");
    			fos.println("#   mob-to-wolf-rate is the tenths of a percent of monster spawns that are replaced with angry wolves");
                fos.println("# If undefined, spawn-anger-rate defaults to 0, mob-to-wolf-rate defaults to 10");
    			fos.println(CONFIG_SPAWN_ANGERRATE + ": 5");
    			fos.println(CONFIG_MOBTOWOLF_RATE + ": 10");
    			fos.println("# If defined, can also have a 'full moon night' one out of every days-per-moon");
    			fos.println("# During this, anger-rate-fullmoon percent of non-tame wolves go angry");
    			fos.println(CONFIG_DAYSPERMOON + ": 28");
    			fos.println(CONFIG_ANGERRATE_MOON +": 25");
    			fos.println(CONFIG_FULLMOONMSG + ": The wolves are baying at the full moon ...");
    			fos.println("# Optional spawn message");
    			fos.println("# spawnmsg: There's a bad moon on the rise...");
    			fos.println("# Wolf-in-sheeps-clothing rate : in 10ths of a percent");
    			fos.println(CONFIG_WOLFINSHEEP_RATE + ": 0");
    			fos.println(CONFIG_WOLFINSHEEP_MSG + ": Oh, no! A wolf in sheep's clothing!");
    			fos.println("# For multi-world specific rates, fill in rate under section for each world");
    			fos.println("worlds:");
    			fos.println("  - name: world");
    			fos.println("    " + CONFIG_SPAWN_ANGERRATE + ": 10");
    			fos.println("    " + CONFIG_MOBTOWOLF_RATE + ": 0");
    			fos.println("    " + CONFIG_DAYSPERMOON + ": 0");
    			fos.println("  - name: transylvania");
    			fos.println("    " + CONFIG_SPAWN_ANGERRATE + ": 90");
    			fos.println("    " + CONFIG_MOBTOWOLF_RATE + ": 100");
    			fos.println("    spawnmsg: Something evil has entered the world...");
    			fos.close();
    		} catch (IOException iox) {
    			System.out.println("ERROR writing default configuration for AngryWolves");
    			return;
    		}
    	}
    	cfg.load();		/* Load it */
    	/* Migrate old settings */
    	boolean dirty = migrateOldSettings(cfg);

    	/* See if we have rates configured */
        def_angerrate = cfg.getInt(CONFIG_SPAWN_ANGERRATE, SPAWN_ANGERRATE_DEFAULT);
        if(def_angerrate < 0) def_angerrate = 0;
        if(def_angerrate > 100) def_angerrate = 100;
        def_spawnmsg = cfg.getString(CONFIG_SPAWNMSG_DEFAULT, null);
        def_mobtowolf_rate = cfg.getInt(CONFIG_MOBTOWOLF_RATE, MOBTOWOLF_RATE_DEFAULT);
        days_per_moon = cfg.getInt(CONFIG_DAYSPERMOON, DAYSPERMOON_DEFAULT);
        def_angerrate_moon = cfg.getInt(CONFIG_ANGERRATE_MOON, ANGERRATE_MOON_DEFAULT);
        def_fullmoonmsg = cfg.getString(CONFIG_FULLMOONMSG, "");
        wolfinsheep_rate = cfg.getInt(CONFIG_WOLFINSHEEP_RATE, WOLFINSHEEP_RATE);
        wolfinsheep_msg = cfg.getString(CONFIG_WOLFINSHEEP_MSG, wolfinsheep_msg);
        /* Now, process world-specific overrides */
        List<ConfigurationNode> w = cfg.getNodeList("worlds", null);
        if(w != null) {
        	for(ConfigurationNode world : w) {
        		String wname = world.getString("name");	/* Get name */
        		if(wname == null)
        			continue;
        		PerWorldState pws = getState(wname);
        		/* Migrate old settings, if needed */
        		dirty = migrateOldSettings(world) || dirty;
        		/* Now load settings */
        		if(world.getProperty(CONFIG_SPAWN_ANGERRATE) != null) {
        			int spawn_ang = world.getInt(CONFIG_SPAWN_ANGERRATE, def_angerrate);
        			if(spawn_ang < 0) spawn_ang = 0;
        			if(spawn_ang > 100) spawn_ang = 100;
        			pws.angerrate = Integer.valueOf(spawn_ang);
        		}
        		String m = world.getString(CONFIG_SPAWNMSG_DEFAULT);
        		if((m != null) && (m.length() > 0)) {
        			pws.spawnmsg = m;
        		}
        		
        		if(world.getProperty(CONFIG_MOBTOWOLF_RATE) != null) {
        			int mobtowolf = world.getInt(CONFIG_MOBTOWOLF_RATE, def_mobtowolf_rate);
        			pws.mobtowolf_rate = Integer.valueOf(mobtowolf);
        		}
        		
        		if(world.getProperty(CONFIG_DAYSPERMOON) != null) {
        			int dpm = world.getInt(CONFIG_DAYSPERMOON, days_per_moon);
        			pws.days_per_moon = Integer.valueOf(dpm);
        		}
        		
        		if(world.getProperty(CONFIG_ANGERRATE_MOON) != null) {
        			int spawn_ang = world.getInt(CONFIG_ANGERRATE_MOON, def_angerrate_moon);
        			if(spawn_ang < 0) spawn_ang = 0;
        			if(spawn_ang > 100) spawn_ang = 100;
        			pws.angerrate_moon = Integer.valueOf(spawn_ang);
        		}

       			m = world.getString(CONFIG_FULLMOONMSG);
       			if((m != null) && (m.length() > 0)) {
       				pws.fullmoonmsg = m;
       			}
       			
       			if(world.getProperty(CONFIG_WOLFINSHEEP_RATE) != null) {
       		        pws.wolfinsheep_rate = cfg.getInt(CONFIG_WOLFINSHEEP_RATE, wolfinsheep_rate);
       			}
       			
       			m = world.getString(CONFIG_WOLFINSHEEP_MSG);
       			if((m != null) && (m.length() > 0)) {
       				pws.wolfinsheep_msg = m;
       			}

        	}
        }
        if(dirty) {	/* If updated, save it */
        	cfg.save();
        }
    }
    
    public int getDaysPerMoonByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	int dpm = days_per_moon;
    	if(pws.days_per_moon != null)
    		dpm = pws.days_per_moon.intValue();
    	//System.out.println("getDaysPerMoonByWorld(" + w.getName() + ")=" + dpm);
    	return dpm;
    }
    
    public int getSpawnRateByWorld(World w) {
    	int v = def_angerrate;    	
    	PerWorldState pws = getState(w.getName());
    	if(pws.angerrate != null)
    		v = pws.angerrate.intValue();
    	/* If full moon, increase rate if appropriate */
    	if(pws.moon_is_full) {
    		int fmr = getFullMoonRateByWorld(w);
    		if(fmr > v) v = fmr;
    	}
    	//System.out.println("getSpawnRateByWorld(" + w.getName() + ")=" + v);
    	return v;
    }

    public String getSpawnMsgByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	String m = def_spawnmsg;
    	if(pws.spawnmsg != null) 
    		m = pws.spawnmsg;
    	//System.out.println("getSpawnMsgByWorld(" + w.getName() + ")=" + m);
    	return m;
    }

    public int getFullMoonRateByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	int v = def_angerrate_moon;
    	if(pws.angerrate_moon != null)
    		v = pws.angerrate_moon.intValue();
    	//System.out.println("getFullMoonRateByWorld(" + w.getName() + ")=" + v);
    	return v;
    }

    public String getFullMoonMsgByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	String m = pws.fullmoonmsg;
    	if(m == null)
    		m = def_fullmoonmsg;
    	//System.out.println("getFullMoonMsgByWorld(" + w.getName() + ")=" + m);
    	return m;
    }

    public int getMobToWolfRateByWorld(World w) {
    	int v = def_mobtowolf_rate;
    	PerWorldState pws = getState(w.getName());
    	if(pws.mobtowolf_rate != null)
    		v = pws.mobtowolf_rate.intValue();
    	//System.out.println("getMobToWolfRateByWorld(" + w.getName() + ")=" + v);
    	return v;
    }
    
    public int getWolfInSheepRateByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	int wisr = wolfinsheep_rate;
    	if(pws.wolfinsheep_rate != null)
    		wisr = pws.wolfinsheep_rate.intValue();
    	return wisr;
    }    
   
    public String getWolfInSheepMsgByWorld(World w) {
    	PerWorldState pws = getState(w.getName());
    	String wism = wolfinsheep_msg;
    	if(pws.wolfinsheep_msg != null)
    		wism = pws.wolfinsheep_msg;
    	return wism;
    }    
    
    /* Wrapper for fact that we don't have proper method for this yet - fix with CB has it */
    public boolean isTame(Wolf w) {
    	boolean tame = false;
    	if(w instanceof CraftWolf) {
    		tame = ((CraftWolf)w).getHandle().y();
    	}
    	return tame;
    }
   
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

	/**
	 * Public API - used to invoke world.spawnCreature for a wolf while preventing it from being angered
	 */
	public LivingEntity spawnNormalWolf(World world, Location loc) {
		boolean last = block_spawn_anger;
		block_spawn_anger = true;
		LivingEntity e = world.spawnCreature(loc, CreatureType.WOLF);
		block_spawn_anger = last;
		return e;
	}

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}
