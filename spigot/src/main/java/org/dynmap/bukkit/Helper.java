package org.dynmap.bukkit;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.dynmap.Log;
import org.dynmap.bukkit.helper.BukkitVersionHelper;

public class Helper {

	private static BukkitVersionHelper loadVersionHelper(String classname) {
		try {
			Class<?> c = Class.forName(classname);
			Constructor<?> cons = c.getConstructor();
			return (BukkitVersionHelper) cons.newInstance();
		} catch (Exception x) {
			Log.severe("Error loading " + classname, x);
			return null;
		}
	}
    public static final BukkitVersionHelper getHelper() {
        if (BukkitVersionHelper.helper == null) {
        	String v = Bukkit.getServer().getMinecraftVersion();
            Log.info("version=" + v);

			if (v.equals("1.21.4")) {
                BukkitVersionHelper.helper = loadVersionHelper("org.dynmap.bukkit.helper.v121.BukkitVersionHelperSpigot121");
            }
        }
        return BukkitVersionHelper.helper;
    }

}
