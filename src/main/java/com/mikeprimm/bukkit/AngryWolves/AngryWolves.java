
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
import java.util.Collections;

/**
 * AngryWolves plugin - watch wolf spawns and make some of them angry by default
 *
 * @author MikePrimm
 */
public class AngryWolves extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");
    private final AngryWolvesEntityListener entityListener = new AngryWolvesEntityListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public boolean verbose = false;
    private int poplimit = ANGRYWOLF_POPLIMIT;
    private double hellhound_dmgscale = HELLHOUND_DMGSCALE;
    private int hellhound_health = HELLHOUND_HEALTH;
    private int angrywolf_health = ANGRYWOLF_HEALTH;
    /* Deprecated parms */
    public static final String CONFIG_ANGERRATE_DEFAULT = "angerrate";
    public static final String CONFIG_ASALTMOB_DEFAULT = "asaltmob";
    /* New parameters */
    public static final String CONFIG_SPAWNMSG_DEFAULT = "spawnmsg";
    public static final String CONFIG_SPAWN_ANGERRATE = "spawn-anger-rate";
    public static final String CONFIG_MOBTOWOLF_RATE = "mob-to-wolf-rate";
    public static final String CONFIG_FULLMOON_MOBTOWOLF_RATE = "fullmoon-mob-to-wolf-rate";
    public static final String CONFIG_CREEPERTOWOLF_RATE = "creeper-to-wolf-rate";
    public static final String CONFIG_ZOMBIETOWOLF_RATE = "zombie-to-wolf-rate";
    public static final String CONFIG_PIGZOMBIETOWOLF_RATE = "pigzombie-to-wolf-rate";
    public static final String CONFIG_SPIDERTOWOLF_RATE = "spider-to-wolf-rate";
    public static final String CONFIG_SKELETONTOWOLF_RATE = "skeleton-to-wolf-rate";
    public static final String CONFIG_DAYSPERMOON = "days-between-fullmoons";
    public static final String CONFIG_ANGERRATE_MOON = "anger-rate-fullmoon";
    public static final String CONFIG_FULLMOONMSG = "fullmoonmsg";
    public static final String CONFIG_WOLFINSHEEP_RATE = "wolf-in-sheep-rate";
    public static final String CONFIG_WOLFINSHEEP_MSG = "wolf-in-sheep-msg";
    public static final String CONFIG_WOLFFRIEND = "wolf-friends";
    public static final String CONFIG_SPAWNMSGRADIUS = "spawnmsgradius";
    public static final String CONFIG_MOBTOWOLF_IGNORE_TERRAIN = "mobtowolf-ignore-terrain";
    public static final String CONFIG_WOLFLOOT = "wolf-loot";
    public static final String CONFIG_WOLFLOOT_RATE = "wolf-loot-rate";
    public static final String CONFIG_WOLFXP = "wolf-xp";
    public static final String CONFIG_ANGRYWOLFLOOT = "angry-wolf-loot";
    public static final String CONFIG_ANGRYWOLFLOOT_RATE = "angry-wolf-loot-rate";
    public static final String CONFIG_ANGRYWOLFXP = "angry-wolf-xp";
    public static final String CONFIG_HELLHOUNDLOOT = "hellhound-loot";
    public static final String CONFIG_HELLHOUNDLOOT_RATE = "hellhound-loot-rate";
    public static final String CONFIG_HELLHOUNDXP = "hellhound-xp";
    public static final String CONFIG_HELLHOUND_RATE = "hellhound-rate";
    public static final String CONFIG_FULLMOON_STAY_ANGRY_RATE = "fullmoon-stay-angry-rate";
    public static final String CONFIG_ANYGYWOLF_POPLIMIT = "angrywolf-pop-limit";
    public static final String CONFIG_HELLHOUND_DAMAGESCALE = "hellhound-damagescale";
    public static final String CONFIG_HELLHOUND_HEALTH = "hellhound-health";
    public static final String CONFIG_ANGRYWOLF_HEALTH = "angrywolf-health";
    public static final String CONFIG_HELLHOUND_FIREBALL_RATE = "hellhound-fireball-rate";
    public static final String CONFIG_HELLHOUND_FIREBALL_RANGE = "hellhound-fireball-range";
    public static final String CONFIG_HELLHOUND_FIREBALL_INCENDIARY = "hellhound-fireball-incendiary";
    
    public static final int SPAWN_ANGERRATE_DEFAULT = 0;
    public static final int MOBTOWOLF_RATE_DEFAULT = 10;
    public static final int DAYSPERMOON_DEFAULT = 28;
    public static final int ANGERRATE_MOON_DEFAULT = 0;
    public static final int WOLFINSHEEP_RATE = 0;
    public static final int SPAWNMSGRADIUS_DEFAULT = 0;	/* Unlimited */
    public static final int HELLHOUND_RATE_DEFAULT = 10;
    public static final int FULLMOON_STAYANGRYRATE_DEFAULT = 0;
    public static final int MOBTOWOLF_RATE_MOON_DEFAULT = -1; /* Use MOBTOWOLF_RATE */
    public static final int ANGRYWOLF_POPLIMIT = 200;
    public static final double HELLHOUND_DMGSCALE = 0.5;
    public static final int HELLHOUND_HEALTH = 20;
    public static final int ANGRYWOLF_HEALTH = 8;
    
    public static final String WOLF_FRIEND_PERM = "angrywolves.wolf-friend";

    /* Common configuration attributes - all tiers */
    public static abstract class BaseConfig {
		String spawnmsg;
    	Integer mobtowolf_rate;
    	Integer fullmoon_mobtowolf_rate;
    	Integer creepertowolf_rate;
    	Integer zombietowolf_rate;
    	Integer pigzombietowolf_rate;
    	Integer skeletontowolf_rate;
    	Integer spidertowolf_rate;
    	Integer hellhound_rate;
    	Integer angerrate;
    	Integer angerrate_moon;
    	Integer stayangryrate_moon;
    	Integer	wolfinsheep_rate;
    	Integer spawnmsgradius;
    	String wolfinsheep_msg;
    	String fullmoonmsg;
       	Boolean wolffriend;  	
       	Boolean ignore_terrain;
       	Integer wolfloot_rate;
       	List<Integer> wolfloot;
        Integer wolfxp;
       	Integer angrywolfloot_rate;
       	List<Integer> angrywolfloot;
        Integer angrywolfxp;
       	Integer hellhoundloot_rate;
       	List<Integer> hellhoundloot;
        Integer hellhoundxp;
       	Integer hellhound_fireball_rate;
        Integer hellhound_fireball_range;
        Boolean hellhound_fireball_incendiary;
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
    	/**
    	 * Resolve mob-type specific rate, if any
    	 * @param t - mob type
    	 * @return rate, or null if not found 
    	 */
    	private Integer getMobSpecWolfRate(CreatureType t) {
    		Integer r = null;
    		switch(t) {
    			case SKELETON:
    				r = skeletontowolf_rate;
    				break;
    			case ZOMBIE:
    				r = zombietowolf_rate;
    				break;
    			case PIG_ZOMBIE:
    				r = pigzombietowolf_rate;
    				break;
    			case SPIDER:
    				r = spidertowolf_rate;
    				break;
    			case CREEPER:
    				r = creepertowolf_rate;
    				break;
    		}
    		if(r == null) {	/* Not defined here? check parents */
        		BaseConfig p = getParent();
        		if(p != null) {
        			r = p.getMobSpecWolfRate(t);
        		}    			
    		}
    		return r;
    	}
    	
    	public int getMobToWolfRate(CreatureType mob, boolean is_fullmoon) {
    		int rate = 0;
    		Integer r = getMobSpecWolfRate(mob);	/* See if specific rate defined */
    		if(r == null) {	/* No? Check for general rate */
    			if(is_fullmoon) {
					rate = mobToWolfRateMoon();
    				if(rate < 0)
						rate = mobToWolfRate();
    			}
    			else {
					rate = mobToWolfRate();
    			}
    		}
    		else {
    			rate = r.intValue();
    		}
    		
    		return rate;
    	}
    	
    	private int mobToWolfRate() {
    		if(mobtowolf_rate != null) {
    			return mobtowolf_rate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.mobToWolfRate();
    		else
    			return MOBTOWOLF_RATE_DEFAULT;    		
    	}

    	private int mobToWolfRateMoon() {
    		if(fullmoon_mobtowolf_rate != null) {
    			return fullmoon_mobtowolf_rate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.mobToWolfRateMoon();
    		else
    			return MOBTOWOLF_RATE_MOON_DEFAULT;    		
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

    	public int getStayAngryRateMoon() {
       		if(stayangryrate_moon != null) {
    			return stayangryrate_moon.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getStayAngryRateMoon();
    		else
    			return FULLMOON_STAYANGRYRATE_DEFAULT;    		
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

    	public int getHellhoundRate() {
       		if(hellhound_rate != null) {
    			return hellhound_rate.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getHellhoundRate();
    		else
    			return HELLHOUND_RATE_DEFAULT;    		
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
    	
       	public String getFullMoonMsg() {
       		if(fullmoonmsg != null)
       			return fullmoonmsg;
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getFullMoonMsg();
       		else
       			return "";
       	}
       	
       	public boolean getWolfFriendActive() {
       		if(wolffriend != null)
       			return wolffriend.booleanValue();
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getWolfFriendActive();
       		else
       			return false;
       	}

        public int getHellhoundFireballRate() {
            if(hellhound_fireball_rate != null)
                return hellhound_fireball_rate.intValue();
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellhoundFireballRate();
            else
                return 0;
        }

        public int getHellhoundFireballRange() {
            if(hellhound_fireball_range != null)
                return hellhound_fireball_range.intValue();
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellhoundFireballRange();
            else
                return 10;
        }

        public boolean getHellhoundFireballIncendiary() {
            if(hellhound_fireball_incendiary != null)
                return hellhound_fireball_incendiary.booleanValue();
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellhoundFireballIncendiary();
            else
                return false;
        }

       	public boolean getMobToWolfTerrainIgnore() {
       		if(ignore_terrain != null)
       			return ignore_terrain.booleanValue();
       		BaseConfig p = getParent();
       		if(p != null)
       			return p.getMobToWolfTerrainIgnore();
       		else
       			return false;
       	}
       	
    	public int getSpawnMsgRadius() {
       		if(spawnmsgradius != null) {
    			return spawnmsgradius.intValue();
    		}
    		BaseConfig p = getParent();
    		if(p != null)
    			return p.getSpawnMsgRadius();
    		else
    			return SPAWNMSGRADIUS_DEFAULT;    		
    	}  	

        public int getWolfLootRate() {
            if(wolfloot_rate != null) {
                return wolfloot_rate.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getWolfLootRate();
            else
                return 0;          
        }   

        public int getWolfXP() {
            if(wolfxp != null) {
                return wolfxp.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getWolfXP();
            else
                return 0;          
        }   

        public List<Integer> getWolfLoot() {
            if(wolfloot != null) {
                return wolfloot;
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getWolfLoot();
            else
                return Collections.singletonList(Integer.valueOf(334));          
        }   

        public int getAngryWolfLootRate() {
            if(angrywolfloot_rate != null) {
                return angrywolfloot_rate.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getAngryWolfLootRate();
            else
                return 0;          
        }   

        public int getAngryWolfXP() {
            if(angrywolfxp != null) {
                return angrywolfxp.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getAngryWolfXP();
            else
                return 0;          
        }   

        public List<Integer> getAngryWolfLoot() {
            if(angrywolfloot != null) {
                return angrywolfloot;
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getAngryWolfLoot();
            else
                return Collections.singletonList(Integer.valueOf(334));          
        }   

        public int getHellHoundLootRate() {
            if(hellhoundloot_rate != null) {
                return hellhoundloot_rate.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellHoundLootRate();
            else
                return -1;	/* Use wolf rate */          
        }   

        public int getHellHoundXP() {
            if(hellhoundxp != null) {
                return hellhoundxp.intValue();
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellHoundXP();
            else
                return -1;  /* Use wolf rate */          
        }   

        public List<Integer> getHellHoundLoot() {
            if(hellhoundloot != null) {
                return hellhoundloot;
            }
            BaseConfig p = getParent();
            if(p != null)
                return p.getHellHoundLoot();
            else
                return null;	/* Use wolf loot */          
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

    		if(n.getProperty(CONFIG_FULLMOON_MOBTOWOLF_RATE) != null) {
    			int mobtowolf = n.getInt(CONFIG_FULLMOON_MOBTOWOLF_RATE, -1);
    			fullmoon_mobtowolf_rate = Integer.valueOf(mobtowolf);
    		}

    		if(n.getProperty(CONFIG_HELLHOUND_RATE) != null) {
    			int hellhoundrate = n.getInt(CONFIG_HELLHOUND_RATE, 0);
    			hellhound_rate = Integer.valueOf(hellhoundrate);
    		}

    		if(n.getProperty(CONFIG_CREEPERTOWOLF_RATE) != null) {
    			int mobrate = n.getInt(CONFIG_CREEPERTOWOLF_RATE, 0);
    			creepertowolf_rate = Integer.valueOf(mobrate);
    		}

    		if(n.getProperty(CONFIG_SKELETONTOWOLF_RATE) != null) {
    			int mobrate = n.getInt(CONFIG_SKELETONTOWOLF_RATE, 0);
    			skeletontowolf_rate = Integer.valueOf(mobrate);
    		}

    		if(n.getProperty(CONFIG_SPIDERTOWOLF_RATE) != null) {
    			int mobrate = n.getInt(CONFIG_SPIDERTOWOLF_RATE, 0);
    			spidertowolf_rate = Integer.valueOf(mobrate);
    		}

    		if(n.getProperty(CONFIG_ZOMBIETOWOLF_RATE) != null) {
    			int mobrate = n.getInt(CONFIG_ZOMBIETOWOLF_RATE, 0);
    			zombietowolf_rate = Integer.valueOf(mobrate);
    		}

    		if(n.getProperty(CONFIG_PIGZOMBIETOWOLF_RATE) != null) {
    			int mobrate = n.getInt(CONFIG_PIGZOMBIETOWOLF_RATE, 0);
    			pigzombietowolf_rate = Integer.valueOf(mobrate);
    		}

    		if(n.getProperty(CONFIG_SPAWNMSGRADIUS) != null) {
    			int smrad = n.getInt(CONFIG_SPAWNMSGRADIUS, 0);
    			spawnmsgradius = Integer.valueOf(smrad);
    		}
    		
    		if(n.getProperty(CONFIG_ANGERRATE_MOON) != null) {
    			int spawn_ang = n.getInt(CONFIG_ANGERRATE_MOON, 0);
    			if(spawn_ang < 0) spawn_ang = 0;
    			if(spawn_ang > 100) spawn_ang = 100;
    			angerrate_moon = Integer.valueOf(spawn_ang);
    		}

    		if(n.getProperty(CONFIG_FULLMOON_STAY_ANGRY_RATE) != null) {
    			int stayangry = n.getInt(CONFIG_FULLMOON_STAY_ANGRY_RATE, FULLMOON_STAYANGRYRATE_DEFAULT);
    			if(stayangry < 0) stayangry = 0;
    			if(stayangry > 100) stayangry = 100;
    			stayangryrate_moon = Integer.valueOf(stayangry);
    		}

   			if(n.getProperty(CONFIG_WOLFINSHEEP_RATE) != null) {
   		        wolfinsheep_rate = n.getInt(CONFIG_WOLFINSHEEP_RATE, 0);
   			}
   			
   			m = n.getString(CONFIG_WOLFINSHEEP_MSG);
   			if((m != null) && (m.length() > 0)) {
   				wolfinsheep_msg = m;
   			}
   			
   			m = n.getString(CONFIG_FULLMOONMSG);
   			if((m != null) && (m.length() > 0)) {
   				fullmoonmsg = m;
   			}

   			if(n.getProperty(CONFIG_WOLFFRIEND) != null) {
   				wolffriend = n.getBoolean(CONFIG_WOLFFRIEND, false);
   			}

            if(n.getProperty(CONFIG_HELLHOUND_FIREBALL_RATE) != null) {
                hellhound_fireball_rate = n.getInt(CONFIG_HELLHOUND_FIREBALL_RATE, 0);
            }
            if(n.getProperty(CONFIG_HELLHOUND_FIREBALL_RANGE) != null) {
                hellhound_fireball_range = n.getInt(CONFIG_HELLHOUND_FIREBALL_RANGE, 10);
            }
            if(n.getProperty(CONFIG_HELLHOUND_FIREBALL_INCENDIARY) != null) {
                hellhound_fireball_incendiary = n.getBoolean(CONFIG_HELLHOUND_FIREBALL_INCENDIARY, false);
            }

   			if(n.getProperty(CONFIG_MOBTOWOLF_IGNORE_TERRAIN) != null) {
   				ignore_terrain = n.getBoolean(CONFIG_MOBTOWOLF_IGNORE_TERRAIN, false);
   			}
   			if(n.getProperty(CONFIG_WOLFLOOT_RATE) != null) {
   			    wolfloot_rate = n.getInt(CONFIG_WOLFLOOT_RATE, 0);
   			}
   			if(n.getProperty(CONFIG_WOLFLOOT) != null) {
   			    wolfloot = n.getIntList(CONFIG_WOLFLOOT, Collections.singletonList(Integer.valueOf(334)));
   			}
            if(n.getProperty(CONFIG_WOLFXP) != null) {
                wolfxp = n.getInt(CONFIG_WOLFXP, 0);
            }
   			if(n.getProperty(CONFIG_ANGRYWOLFLOOT_RATE) != null) {
   			    angrywolfloot_rate = n.getInt(CONFIG_ANGRYWOLFLOOT_RATE, -1);
   			}
   			if(n.getProperty(CONFIG_ANGRYWOLFLOOT) != null) {
   			    angrywolfloot = n.getIntList(CONFIG_ANGRYWOLFLOOT, null);
   			}
            if(n.getProperty(CONFIG_ANGRYWOLFXP) != null) {
                angrywolfxp = n.getInt(CONFIG_ANGRYWOLFXP, 0);
            }
   			if(n.getProperty(CONFIG_HELLHOUNDLOOT_RATE) != null) {
   			    hellhoundloot_rate = n.getInt(CONFIG_HELLHOUNDLOOT_RATE, -1);
   			}
   			if(n.getProperty(CONFIG_HELLHOUNDLOOT) != null) {
   			    hellhoundloot = n.getIntList(CONFIG_HELLHOUNDLOOT, null);
   			}
            if(n.getProperty(CONFIG_HELLHOUNDXP) != null) {
                hellhoundxp = n.getInt(CONFIG_HELLHOUNDXP, 0);
            }
    	}
    	public String toString() {
    		return "spawnmsg=" + this.getSpawnMsg() +
    		    ", spawnmsgradius=" + this.getSpawnMsgRadius() + 
    		    ", spawnrate=" + this.getSpawnAngerRate() + 
    			", wolfinsheeprate=" + this.getWolfInSheepRate() +
    			", wolfinsheepmsg=" + this.getWolfInSheepMsg() +
    			", angerratemoon=" + this.getSpawnAngerRateMoon() +
    			", fullmoonmsg=" + getFullMoonMsg() +
    			", wolffriend=" + getWolfFriendActive();
    	}
    };
    
    /* World-level configuration attributes */
    public static class WorldConfig extends BaseConfig {
    	/* World-specific configuration attributes */
    	Integer days_per_moon;
       	
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
       	
    	void loadConfiguration(ConfigurationNode n) {
    		super.loadConfiguration(n);	/* Load base attributes */

    		if(n.getProperty(CONFIG_DAYSPERMOON) != null) {
    			int dpm = n.getInt(CONFIG_DAYSPERMOON, 0);
    			days_per_moon = Integer.valueOf(dpm);
    		}

    	}
    	
    	public String toString() {
    		return "dayspermoon=" + getDaysPerMoon() +
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
    	double x[];
    	double z[];
    	/* Derived coords - bounding box, for fast dismissal during intersect test */
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
    		/* Get coordinate list */
            List<ConfigurationNode> cl = n.getNodeList("coords", null);
            if(cl != null) {
            	int len = cl.size();	/* Get number of elements in list */
            	x = new double[len];
            	z = new double[len];
            	int i = 0;
            	for(ConfigurationNode coord : cl) {	/* Loop through coords */
            		x[i] = coord.getDouble("x", 0.0);
            		z[i] = coord.getDouble("z", 0.0);	/* Read coordinates into array */
            		if(i > 0) {	/* Compute bounding box */
            			if(x[i] < x_low) x_low = x[i];
            			if(x[i] > x_high) x_high = x[i];
            			if(z[i] < z_low) z_low = z[i];
            			if(z[i] > z_high) z_high = z[i];
            		}
            		else {
            			x_low = x_high = x[i];
            			z_low = z_high = z[i];
            		}
            		i++;
            	}
            }
    	}

    	/* Test if given coordinates are inside our area - assumes world already checked */
    	public boolean isInArea(double xval, double yval, double zval) {
    		/* Do bounding box test first - fastest dismiss */
    		if((xval < x_low) || (xval > x_high) || (zval < z_low) || (zval > z_high)) {
    			return false;
    		}
    		/* Now, if we have 3 or more points, test if within the polygon too */
    		if(x.length > 2) {
    			/* Winding test - count edges looking towards -z */
    			int i, j;
    			boolean odd = false;
    			for(i = 0, j = x.length-1; i < x.length; j = i++) {
    				/* If x within range of next line segment, and z is to left of intersec at x=xval, hit */
    				if( ((x[i] > xval) != (x[j] > xval)) &&
    						(zval < (z[j]-z[i]) * (xval-x[i]) / (x[j] - x[i]) + z[i]) )
    					odd = !odd;
    			}
    			return odd;	/* If odd, we're inside */
    		}
    		else	/* If 2 points, bounding box is enough */
    			return true;
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
    
    public boolean isFullMoon(World w) {
    	PerWorldState pws = getState(w.getName());
    	return pws.moon_is_full;
    }
    
    /**
     *  Find configuration record, by world and coordinates
     * @param loc - location
     * @return first matching config record
     */
    public BaseConfig findByLocation(Location loc) {
    	PerWorldState pws = getState(loc.getWorld().getName());
    	if(pws.areas != null) {	/* Any areas? */
    		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
    		for(AreaConfig ac : pws.areas) {
    			/* If location is within area's rectangle */
    			if(ac.isInArea(x, y, z)) {
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
    		if(verbose) log.info("Check full moon");
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
    						if(verbose) log.info("Starting full moon in " + world.getName());
    						/* And handle event */
    						List<Player> pl = world.getPlayers();
    						for(Player p : pl) {
    							AngryWolves.BaseConfig pc = findByLocation(p.getLocation());
    							String msg = pc.getFullMoonMsg();
    							/* Send the message to the player, if there is one */
    							if((msg != null) && (msg.length() > 0)) {
    								p.sendMessage(msg);
    							}
    						}
    						/* And make the wolves angry */
    						List<LivingEntity> lst = world.getLivingEntities();
    						for(LivingEntity le : lst) {
    							if(le instanceof Wolf) {
    								Wolf wolf = (Wolf)le;
    								/* If not angry and not tame, make angry */
    								if((wolf.isAngry() == false) && (isTame(wolf) == false)) {
    									/* Check situation at wolf's location */
    									AngryWolves.BaseConfig wc = findByLocation(wolf.getLocation());
										if(rnd.nextInt(100) < wc.getSpawnAngerRateMoon()) {
											wolf.setAngry(true);
											if(verbose) log.info("Made wolf angry (full moon)");
										}
    								}
    							}
    						}
    					}
    				}
    				else if(pws.moon_is_full) {	/* Was full, but over now */ 
    					pws.moon_is_full = false;
    					if(verbose) log.info("Full moon ended in " + world.getName());
						/* And make the wolves happy */
    					List<LivingEntity> lst = world.getLivingEntities();
						for(LivingEntity le : lst) {
							if(le instanceof Wolf) {
								Wolf wolf = (Wolf)le;
								/* If angry and not a hellhound, make not angry */
								if(wolf.isAngry() && (!AngryWolvesEntityListener.isHellHound(wolf))) {
									/* If wolf's location is where moon anger happens, clear anger */
									BaseConfig loc = findByLocation(wolf.getLocation());
									if(loc.getSpawnAngerRateMoon() > 0) {
										if(rnd.nextInt(100) >= loc.getStayAngryRateMoon()) {
											wolf.setAngry(false);
											wolf.setTarget(null);
											if(verbose) log.info("Made angry wolf calm (end-of-moon)");
										}
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
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[AngryWolves] version " + pdfFile.getVersion() + " is enabled" );
        /* Start job to watch for sunset/sunrise (every 30 seconds or so) */
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckForMoon(), 0, 20*30);
    }
    
    private void readConfig() {    	
    	File configdir = getDataFolder();	/* Get our data folder */
    	if(configdir.exists() == false) {	/* Not yet defined? */
    		configdir.mkdirs();				/* Create it */
    	}
    	File configfile = new File(configdir, "AngryWolves.yml");	/* Our YML file */
    	Configuration cfg = new Configuration(configfile);
    	if(configfile.exists() == false) {	/* Not defined yet? */
    		PrintWriter fos = null;
    		try {
    			fos = new PrintWriter(new FileWriter(configfile));
    			fos.println("# Configuration file for AngryWolves);");
    			fos.println("#   spawn-anger-rate is percentage of normal wolf spawns that spawn angry");
    			fos.println("# If undefined, spawn-anger-rate defaults to 0");
    			fos.println(CONFIG_SPAWN_ANGERRATE + ": 5");
    			fos.println("#   hellhound-rate is percentage of angry wolfs that are hellhounds (flaming-fireproof-wolves)");
    			fos.println("# If undefined, hellhound-rate defaults to 10.  In Nether, 100% of angry wolves are hellhounds.");
    			fos.println(CONFIG_HELLHOUND_RATE + ": 10");
    			fos.println("#   mob-to-wolf-rate is the TENTHS of a percent of monster spawns that are replaced with angry wolves");
    			fos.println("#   spider-to-wolf-rate is the TENTHS of a percent of spider spawns that are replaced with angry wolves");
    			fos.println("#   zombie-to-wolf-rate is the TENTHS of a percent of zombie spawns that are replaced with angry wolves");
    			fos.println("#   skeleton-to-wolf-rate is the TENTHS of a percent of skeleton spawns that are replaced with angry wolves");
    			fos.println("#   creeper-to-wolf-rate is the TENTHS of a percent of creeper spawns that are replaced with angry wolves");
    			fos.println("#   pig-zombie-to-wolf-rate is the TENTHS of a percent of pig-zombie spawns that are replaced with angry wolves");
    			fos.println("#   note: if monster type specific rate is defined, it supercedes the mob-to-wolf-rate for that monster type");
                fos.println("# If undefined, mob-to-wolf-rate defaults to 10, others are null");
    			fos.println(CONFIG_MOBTOWOLF_RATE + ": 10");
    			fos.println("# " + CONFIG_SPIDERTOWOLF_RATE + ": 20");
    			fos.println("# " + CONFIG_ZOMBIETOWOLF_RATE + ": 0");
    			fos.println("# " + CONFIG_SKELETONTOWOLF_RATE + ": 5");
    			fos.println("# " + CONFIG_CREEPERTOWOLF_RATE + ": 1000");
    			fos.println("# " + CONFIG_PIGZOMBIETOWOLF_RATE + ": 20");
    			fos.println("# mob-to-spawn-based spawns are normally limited to spawns occuring in valid biomes for wolves, as well as over valid wolf spawn terrain (grass)");
    			fos.println("# " + CONFIG_MOBTOWOLF_IGNORE_TERRAIN + " can be set to 'true' to disable biome and terrain restrictions");
    			fos.println("# " + CONFIG_MOBTOWOLF_IGNORE_TERRAIN + ": true");
    			fos.println("# If defined, can also have a 'full moon night' one out of every days-per-moon");
    			fos.println("# During this, anger-rate-fullmoon percent of non-tame wolves go angry");
    			fos.println("# At the end of the full moon, fullmoon-stay-angry-rate percent of angry wolves stay angry");
    			fos.println(CONFIG_DAYSPERMOON + ": 28");
    			fos.println(CONFIG_ANGERRATE_MOON +": 25");
    			fos.println(CONFIG_FULLMOONMSG + ": The wolves are baying at the full moon ...");
    			fos.println(CONFIG_FULLMOON_STAY_ANGRY_RATE + ": 0");
    			fos.println("# Optional - mob-to-wolf-rate to apply during full moon (if set - otherwise, same rate used)");
    			fos.println("# " + CONFIG_FULLMOON_MOBTOWOLF_RATE + ": 50");
    			fos.println("# Optional spawn message");
    			fos.println("# spawnmsg: There's a bad moon on the rise...");
    			fos.println("# Also, optional spawn message radius - limits message to only players within given number of blocks of spawn");
    			fos.println("# spawnmsgradius: 50");
    			fos.println("# Wolf-in-sheeps-clothing rate : in 10ths of a percent");
    			fos.println(CONFIG_WOLFINSHEEP_RATE + ": 0");
    			fos.println(CONFIG_WOLFINSHEEP_MSG + ": Oh, no! A wolf in sheep's clothing!");
    			fos.println("# Optional - enable 'wolf-friends' : players with the 'angrywolves.wolf-friend' privilege will not be targetted by angry wolves!");
    			fos.println("# wolf-friends: true");
    			fos.println("# Optional - enable wolf loot - wolf-loot-rate is percent change of drop, wolf-loot is list of item ids to select from (1 randomly picked), wolf-xp is experience orbs dropped");
    			fos.println("# " + CONFIG_WOLFLOOT_RATE + ": 20");
    			fos.println("# " + CONFIG_WOLFLOOT + ": [ 334, 352, 319 ]");
                fos.println("# " + CONFIG_WOLFXP + ": 5");
    			fos.println("# Optional - enable different loot for angry wolves (if not defined, wolf loot settings are used)");
    			fos.println("# " + CONFIG_ANGRYWOLFLOOT_RATE + ": 70");
    			fos.println("# " + CONFIG_ANGRYWOLFLOOT + ": [ 334, 352, 319 ]");
                fos.println("# " + CONFIG_ANGRYWOLFXP + ": 5");
    			fos.println("# Optional - enable different loot for hellhounds (if not defined, wolf loot settings are used)");
    			fos.println("# " + CONFIG_HELLHOUNDLOOT_RATE + ": 90");
    			fos.println("# " + CONFIG_HELLHOUNDLOOT + ": [ 334, 352, 319 ]");
    			fos.println("# " + CONFIG_HELLHOUNDXP + ": 10");
    			fos.println("# Population limit for Angry Wolves and Hellhounds (combined for server)");
    			fos.println("anygrywolf-pop-limit: 200");
    			fos.println("# Angry Wolf initial health - normal wild wolves are 8, tamed wolves are 20");
    			fos.println("angrywolf-health: 8");
    			fos.println("# Hellhound initial health - normal wild wolves are 8, tamed wolves are 20");
    			fos.println("hellhound-health: 10");
    			fos.println("# Hellhound damage scale - multiplier for general damage to hellhounds (less that 1.0 reduces damage done to them)");
    			fos.println("hellhound-damagescale: 0.5");
    			fos.println("# (optional) have hellhounds shoot fireballs!  Control range, rate (seconds between shots), and whether they cause fires");
    			fos.println("#hellhound-fireball-range: 10");
                fos.println("#hellhound-fireball-rate: 3");
                fos.println("#hellhound-fireball-incendiary: false");
    			fos.println("# For multi-world specific rates, fill in rate under section for each world");
    			fos.println("worlds:");
    			fos.println("#  - name: world");
    			fos.println("#    " + CONFIG_SPAWN_ANGERRATE + ": 10");
    			fos.println("#    " + CONFIG_MOBTOWOLF_RATE + ": 0");
    			fos.println("#    " + CONFIG_DAYSPERMOON + ": 0");
    			fos.println("#  - name: transylvania");
    			fos.println("#    " + CONFIG_SPAWN_ANGERRATE + ": 90");
    			fos.println("#    " + CONFIG_MOBTOWOLF_RATE + ": 100");
    			fos.println("#    spawnmsg: Something evil has entered the world...");
    			fos.println("# Optional - for special settings limited to an area on one world");
    			fos.println("#  'coords' define the area, as a list of two or more coordinate values (each of which has an x and z attribute).");
    			fos.println("areas:");
    			fos.println("#  - name: Area51");
    			fos.println("#    worldname: world");
    			fos.println("#    coords:");
    			fos.println("#      - x: 200");
    			fos.println("#        z: 40");
    			fos.println("#      - x: 60");
    			fos.println("#        z: 100");
       			fos.println("#    " + CONFIG_SPAWN_ANGERRATE + ": 100");
    			fos.println("#    " + CONFIG_MOBTOWOLF_RATE + ": 100");
    			fos.close();
    		} catch (IOException iox) {
    			log.severe("ERROR writing default configuration for AngryWolves");
    			return;
    		}
    	}
    	cfg.load();		/* Load it */
    	boolean dirty = false;

    	/* Load default world-level configuration */
    	def_config = new WorldConfig(null);	/* Make base default object */
    	def_config.loadConfiguration(cfg);
    	//log.info("defconfig: " + def_config);
    	verbose = cfg.getBoolean("verbose", false);
    	poplimit = cfg.getInt(CONFIG_ANYGYWOLF_POPLIMIT, ANGRYWOLF_POPLIMIT);
    	hellhound_dmgscale = cfg.getDouble(CONFIG_HELLHOUND_DAMAGESCALE, HELLHOUND_DMGSCALE);
    	if(hellhound_dmgscale < 0) hellhound_dmgscale = 0.0;
    	hellhound_health = cfg.getInt(CONFIG_HELLHOUND_HEALTH, HELLHOUND_HEALTH);
    	if(hellhound_health < 1) hellhound_health = 1;
    	angrywolf_health = cfg.getInt(CONFIG_ANGRYWOLF_HEALTH, ANGRYWOLF_HEALTH);
    	if(angrywolf_health < 1) angrywolf_health = 1;
    	
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
        		/* Now load settings */
        		pws.loadConfiguration(world);
        		//log.info("world " + wname + ": " + pws);
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
        		//log.info("area " + aname + "/" + wname + ": " + ac);
        	}
        }

        if(dirty) {	/* If updated, save it */
        	cfg.save();
        }
    }

    public boolean isTame(Wolf w) {
    	return w.isTamed();
    }
   
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public int getPopulationLimit() {
    	return poplimit;
    }
    public double getHellhoundDamageScale() {
    	return hellhound_dmgscale;
    }
    public int getHellhoundHealth() {
    	return hellhound_health;
    }
    public int getAngryWolfHealth() {
    	return angrywolf_health;
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
