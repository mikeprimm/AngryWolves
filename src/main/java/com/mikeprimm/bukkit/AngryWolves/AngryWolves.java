
package com.mikeprimm.bukkit.AngryWolves;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkit.World;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;

/**
 * AngryWolves plugin - watch wolf spawns and make some of them angry by default
 *
 * @author MikePrimm
 */
public class AngryWolves extends JavaPlugin {
    private final AngryWolvesEntityListener entityListener = new AngryWolvesEntityListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

    public static final String CONFIG_ANGERRATE_DEFAULT = "angerrate";
    public static final int ANGERRATE_DEFAULT = 10;
    public static final String CONFIG_SPAWNMSG_DEFAULT = "spawnmsg";
    
    private HashMap<String, Integer> anger_by_world = new HashMap<String,Integer>();
    private int def_anger = ANGERRATE_DEFAULT;
    private HashMap<String, String> spawnmsg_by_world = new HashMap<String, String>();
    private String def_spawnmsg = null;
    
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
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    private void readConfig() {
    	boolean dirty = false;	/* If set, need to write config when done */
    	
    	File configdir = getDataFolder();	/* Get our data folder */
    	if(configdir.exists() == false) {	/* Not yet defined? */
    		configdir.mkdirs();				/* Create it */
    		dirty = true;
    	}
    	/* Initialize configuration object */
    	File configfile = new File(configdir, "AngryWolves.yml");	/* Our YML file */
    	Configuration cfg = new Configuration(configfile);
    	if(configfile.exists() == false) {	/* Not defined yet? */
    		PrintWriter fos = null;
    		try {
    			fos = new PrintWriter(new FileWriter(configfile));
    			fos.println("# Configuration file for AngryWolves - anger-rate is percent of spawned wolves that are angry");
    			fos.println("angerrate: 5");
    			fos.println("spawnmsg: A wolf has answered the call of the wild...");
    			
    			fos.println("# For multi-world specific rates, fill in rate under section for each world");
    			fos.println("worlds:");
    			fos.println("  - name: world");
    			fos.println("    angerrate: 10");
    			fos.println("  - name: transylvania");
    			fos.println("    angerrate: 90");
    			fos.println("    spawnmsg: Something evil has entered the world...");
    			fos.close();
    		} catch (IOException iox) {
    			System.out.println("ERROR writing default configuration for AngryWolves");
    			return;
    		}
    	}
    	cfg.load();		/* Load it */
        /* See if we have rates configured */
        def_anger = cfg.getInt(CONFIG_ANGERRATE_DEFAULT, ANGERRATE_DEFAULT);
        if(def_anger < 0) def_anger = 0;
        if(def_anger > 100) def_anger = 100;
        def_spawnmsg = cfg.getString(CONFIG_SPAWNMSG_DEFAULT, null);
        
        /* Now, process world-specific overrides */
        Object w = cfg.getProperty("worlds");
        if((w != null) && (w instanceof List)) {
        	for(Object wrld : (List)w) {
        		Map<String, Object>world = (Map<String,Object>) wrld;
        		String wname = (String)world.get("name");	/* Get name */
        		Integer v = (Integer)world.get(CONFIG_ANGERRATE_DEFAULT); /* Get value */
        		if((wname != null) || (v != null)) {
        			int vv = v.intValue();
        			if(vv < 0) vv = 0;
        			if(vv > 100) vv = 100;
        			anger_by_world.put(wname, Integer.valueOf(vv));
        		}
        		String m = (String)world.get(CONFIG_SPAWNMSG_DEFAULT);
        		if((m != null) && (m.length() > 0)) {
        			spawnmsg_by_world.put(wname, m);
        		}
        	}
        }
    }

    public int getRateByWorld(World w) {
    	int v = def_anger;
    	Integer r = anger_by_world.get(w.getName());
    	if(r != null)
    		v = r.intValue();
    	System.out.println("getRateByWorld(" + w.getName() + ") = " + v);
    	
    	return v;
    }

    public String getSpawnMsgByWorld(World w) {
    	String m = spawnmsg_by_world.get(w.getName());
    	if(m == null)
    		m = def_spawnmsg;
    	System.out.println("getSpawnMsgByWorld(" + w.getName() + ") = " + m);
    	
    	return m;
    }

    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}
