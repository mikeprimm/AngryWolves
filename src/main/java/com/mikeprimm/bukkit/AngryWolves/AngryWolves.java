
package com.mikeprimm.bukkit.AngryWolves;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;
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
	public static Logger log = Logger.getLogger("Minecraft");
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
    public static final String CONFIG_WOLFFRIEND = "wolf-friends";
    public static final int SPAWN_ANGERRATE_DEFAULT = 0;
    public static final int MOBTOWOLF_RATE_DEFAULT = 10;
    public static final int DAYSPERMOON_DEFAULT = 28;
    public static final int ANGERRATE_MOON_DEFAULT = 0;
    public static final int WOLFINSHEEP_RATE = 0;
    
    public static final String WOLF_FRIEND_PERM = "angrywolves.wolf-friend";

    /* Common configuration attributes - all tiers */
    public static abstract class BaseConfig {
    	String spawnmsg;
    	Integer mobtowolf_rate;
    	Integer angerrate;
    	Integer angerrate_moon;
    	Integer	wolfinsheep_rate;
    	String wolfinsheep_msg;
    	
    	abstract BaseConfig getParent();
    	
    	public String getSpawnMsg() {
    		if(spawnmsg != null) {
    			return spawnmsg;
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getSpawnMsg();
    		else
    			return "";
    	}
    	
    	public int getMobToWolfRate() {
    		if(mobtowolf_rate != null) {
    			return mobtowolf_rate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getMobToWolfRate();
    		else
    			return MOBTOWOLF_RATE_DEFAULT;    		
    	}
    	
    	public int getSpawnAngerRate() {
       		if(angerrate != null) {
    			return angerrate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getSpawnAngerRate();
    		else
    			return SPAWN_ANGERRATE_DEFAULT;    		
    	}

    	public int getSpawnAngerRateMoon() {
       		if(angerrate_moon != null) {
    			return angerrate_moon.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getSpawnAngerRateMoon();
    		else
    			return ANGERRATE_MOON_DEFAULT;    		
    	}

    	
    	public int getWolfInSheepRate() {
       		if(wolfinsheep_rate != null) {
    			return wolfinsheep_rate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getWolfInSheepRate();
    		else
    			return WOLFINSHEEP_RATE;    		
    	}

    	public String getWolfInSheepMsg() {
    		if(wolfinsheep_msg != null) {
    			return wolfinsheep_msg;
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getWolfInSheepMsg();
    		else
    			return "";
    	}

    	void loadConfiguration(ConfigurationNode n) {
    		if(n.getProperty(CONFIG_SPAWN_ANGERRATE) != null) {
    			int spawn_ang = n.getInt(CONFIG_SPAWN_ANGERRATE, 0);
    			if(spawn_ang < 0) spawn_ang = 0;
    			if(spawn_ang > 100) spawn_ang = 100;
    			angerrate = Integer.valueOf(spawn_ang);
    		}
    		String m = n.getString(CONFIG_SPAWNMSG_DEFAULT);
    		if((m != null) && (m.length() > 0)) {
    			spawnmsg = m;
    		}
    		
    		if(n.getProperty(CONFIG_MOBTOWOLF_RATE) != null) {
    			int mobtowolf = n.getInt(CONFIG_MOBTOWOLF_RATE, 0);
    			mobtowolf_rate = Integer.valueOf(mobtowolf);
    		}
    		
    		if(n.getProperty(CONFIG_ANGERRATE_MOON) != null) {
    			int spawn_ang = n.getInt(CONFIG_ANGERRATE_MOON, 0);
    			if(spawn_ang < 0) spawn_ang = 0;
    			if(spawn_ang > 100) spawn_ang = 100;
    			angerrate_moon = Integer.valueOf(spawn_ang);
    		}
   			
   			if(n.getProperty(CONFIG_WOLFINSHEEP_RATE) != null) {
   		        wolfinsheep_rate = n.getInt(CONFIG_WOLFINSHEEP_RATE, 0);
   			}
   			
   			m = n.getString(CONFIG_WOLFINSHEEP_MSG);
   			if((m != null) && (m.length() > 0)) {
   				wolfinsheep_msg = m;
   			}
    	}
    	public String toString() {
    		return "spawnmsg=" + this.getSpawnMsg() +
    		    ", spawnrate=" + this.getSpawnAngerRate() + 
    			", wolfinsheeprate=" + this.getWolfInSheepRate() +
    			", wolfinsheepmsg=" + this.getWolfInSheepMsg() +
    			", angerratemoon=" + this.getSpawnAngerRateMoon() +
    			", mobtowolfrate=" + this.getMobToWolfRate();
    	}
    };
    
    /* World-level configuration attributes */
    public static class WorldConfig extends BaseConfig {
    	/* World-specific configuration attributes */
    	Integer days_per_moon;
    	String fullmoonmsg;
       	Boolean wolffriend;
       	
       	WorldConfig par;
       	
       	WorldConfig(WorldConfig p) {
       		par = p;
       	}
       	
       	BaseConfig getParent() { return par; }
       	
       	public int getDaysPerMoon() {
       		if(days_per_moon != null) {
       			return days_per_moon.intValue();
       		}
       		if(par != null)
       			return par.getDaysPerMoon();
       		else
       			return DAYSPERMOON_DEFAULT;
       	}
       	
       	public String getFullMoonMsg() {
       		if(fullmoonmsg != null)
       			return fullmoonmsg;
       		if(par != null)
       			return par.getFullMoonMsg();
       		else
       			return "";
       	}
       	
       	public boolean getWolfFriendActive() {
       		if(wolffriend != null)
       			return wolffriend.booleanValue();
       		if(par != null)
       			return par.getWolfFriendActive();
       		else
       			return false;
       	}
       	
    	void loadConfiguration(ConfigurationNode n) {
    		super.loadConfiguration(n);	/* Load base attributes */

    		if(n.getProperty(CONFIG_DAYSPERMOON) != null) {
    			int dpm = n.getInt(CONFIG_DAYSPERMOON, 0);
    			days_per_moon = Integer.valueOf(dpm);
    		}

   			String m = n.getString(CONFIG_FULLMOONMSG);
   			if((m != null) && (m.length() > 0)) {
   				fullmoonmsg = m;
   			}

   			if(n.getProperty(CONFIG_WOLFFRIEND) != null) {
   				wolffriend = n.getBoolean(CONFIG_WOLFFRIEND, false);
   			}
    	}
    	
    	public String toString() {
    		return "dayspermoon=" + getDaysPerMoon() +
    			", fullmoonmsg=" + getFullMoonMsg() +
    			", wolffriend=" + getWolfFriendActive() +
    			", " + super.toString();
    	}
    };
    
    /* World state records - include config and operating state */
    private static class PerWorldState extends WorldConfig {
    	boolean moon_is_full;
    	int	daycounter;
    	long	last_time;
    	List<AreaConfig> areas;
    	
    	PerWorldState() {
    		super(def_config);
    	}
    };

    /* Area level configuration attributes */
    public static class AreaConfig extends BaseConfig {
    	String areaname;
    	double x_low, x_high;
    	double z_low, z_high;
    	WorldConfig par;
    	
    	BaseConfig getParent() { return par; }

    	AreaConfig(String n, WorldConfig p) {
    		par = p;
    		areaname = n;
    	}
    	
    	void loadConfiguration(ConfigurationNode n) {
    		super.loadConfiguration(n);	/* Load base attributes */

    		/* Get coordinates */
    		x_low = n.getDouble("xlow", 0);
    		x_high = n.getDouble("xhigh", 0);
    		z_low = n.getDouble("zlow", 0);
    		z_high = n.getDouble("zhigh", 0);
    		/* Fix ordering, if needed */
    		double tmp;
    		if(x_low > x_high) { tmp = x_low; x_low = x_high; x_high = tmp; }
    		if(z_low > z_high) { tmp = z_low; z_low = z_high; z_high = tmp; }
    	}
    	
    	public String toString() {
    		return "name=" + areaname +
    		    ", xlow=" + x_low + ", xhigh=" + x_high + ", zlow=" + z_low  + ", zhigh=" + z_high +
    		    ", " + super.toString();
    	}
    };

    private static HashMap<String, PerWorldState> per_world = new HashMap<String, PerWorldState>();
    
    private static WorldConfig def_config;	/* Base world configuration */
        
    private boolean block_spawn_anger = false;	/* Used for anger-free spawn */
    
    private Random rnd = new Random(System.currentTimeMillis());
    
    private static PerWorldState getState(String w) {
    	PerWorldState pws = per_world.get(w);
    	if(pws == null) {
    		pws = new PerWorldState();
    		per_world.put(w, pws);
    	}
    	return pws;
    }
    
    /**
     *  Find configuration record, by world and coordinates
     * @param loc - location
     * @return first matching config record
     */
    public BaseConfig findByLocation(Location loc) {
    	PerWorldState pws = getState(loc.getWorld().getName());
    	if(pws.areas != null) {	/* Any areas? */
    		for(AreaConfig ac : pws.areas) {
    			/* If location is within area's rectangle */
    			if((ac.x_low <= loc.getX()) && (ac.x_high >= loc.getX()) &&
					(ac.z_low <= loc.getZ()) && (ac.z_high >= loc.getZ())) {
    				return ac;
    			}
    		}
    	}
    	return pws;
    }
    /**
     * Find configuration record by world
     * @param w - world
     * @return first matching config record
     */
    public WorldConfig findByWorld(World w) {
    	return getState(w.getName());
    }
    
    boolean isNormalSpawn() {
    	return block_spawn_anger;
    }
    
    private class CheckForMoon implements Runnable {
    	public void run() {
    		List<World> w = getServer().getWorlds();
    		for(World world : w) {
    			PerWorldState pws = getState(world.getName());
    			
    			int dpm = pws.getDaysPerMoon();	/* Get lunar period */
    			if(dpm <= 0) {	/* Disabled? */
    				pws.moon_is_full = false; /* Not us */
    			}
    			else {    				
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
    						String msg = pws.getFullMoonMsg();
    						if((msg != null) && (msg.length() > 0)) {
    							List<Player> pl = world.getPlayers();
    							for(Player p : pl) {
    								p.sendMessage(msg);
    							}
    						}
    						/* And make the wolves angry */
    						int rate = pws.getSpawnAngerRateMoon();
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
						int rate = pws.getSpawnAngerRateMoon();
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
    	/* Initialize our permissions */
    	AngryWolvesPermissions.initialize(getServer());
    	
    	/* Read in our configuration */
        readConfig();

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
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
    			fos.println("# Optional - enable 'wolf-friends' : players with the 'angrywolves.wolf-friend' privilege will not be targetted by angry wolves!");
    			fos.println("# wolf-friends: true");
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
    			fos.println("# Optional - for special settings limited to a rectangular area on one world");
    			fos.println("#  'coords' are integer block coordinates: xlow, xhigh, zlow, zhigh");
    			fos.println("areas:");
    			fos.println("  - name: Area51");
    			fos.println("    worldname: world");
    			fos.println("    xlow: 5");
    			fos.println("    xhigh: 200");
    			fos.println("    zlow: 40");
    			fos.println("    zhigh: 100");
       			fos.println("    " + CONFIG_SPAWN_ANGERRATE + ": 100");
    			fos.println("    " + CONFIG_MOBTOWOLF_RATE + ": 100");
    			fos.close();
    		} catch (IOException iox) {
    			System.out.println("ERROR writing default configuration for AngryWolves");
    			return;
    		}
    	}
    	cfg.load();		/* Load it */
    	/* Migrate old settings */
    	boolean dirty = migrateOldSettings(cfg);

    	/* Load default world-level configuration */
    	def_config = new WorldConfig(null);	/* Make base default object */
    	def_config.loadConfiguration(cfg);
    	//System.out.println("defconfig: " + def_config);
    	
    	/* Now, process world-specific overrides */
        List<ConfigurationNode> w = cfg.getNodeList("worlds", null);
        if(w != null) {
        	for(ConfigurationNode world : w) {
        		String wname = world.getString("name");	/* Get name */
        		if(wname == null)
        			continue;
        		/* Load/initialize per world state/config */
        		PerWorldState pws = getState(wname);
        		pws.par = def_config;	/* Our parent is global default */
        		/* Migrate old settings, if needed */
        		dirty = migrateOldSettings(world) || dirty;
        		/* Now load settings */
        		pws.loadConfiguration(world);
        		//System.out.println("world " + wname + ": " + pws);
        	}
        }
        /* Now, process area-specific overrides */
        w = cfg.getNodeList("areas", null);
        if(w != null) {
        	for(ConfigurationNode area : w) {
        		String aname = area.getString("name");	/* Get name */
        		if(aname == null)
        			continue;
        		String wname = area.getString("worldname");	/* Get world name */
        		if(wname == null)
        			continue;
        		PerWorldState pws = getState(wname);		/* Look up world state */
        		/* Now, make our area state */
        		AreaConfig ac = new AreaConfig(aname, pws);

        		if(pws.areas == null) pws.areas = new ArrayList<AreaConfig>();
        		pws.areas.add(ac);	/* Add us to our world */
        		/* Now load settings */
        		ac.loadConfiguration(area);
        		//System.out.println("area " + aname + "/" + wname + ": " + ac);
        	}
        }

        if(dirty) {	/* If updated, save it */
        	cfg.save();
        }
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
