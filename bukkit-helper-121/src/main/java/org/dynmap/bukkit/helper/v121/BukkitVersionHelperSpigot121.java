package org.dynmap.bukkit.helper.v121;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.dynmap.DynmapChunk;
import org.dynmap.Log;
import org.dynmap.bukkit.helper.BukkitVersionHelper;
import org.dynmap.bukkit.helper.BukkitWorld;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.Polygon;

import net.minecraft.core.RegistryBlockID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.level.BlockAccessAir;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.state.IBlockData;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Helper for isolation of bukkit version specific issues
 */
public class BukkitVersionHelperSpigot121 extends BukkitVersionHelper {
    private static IRegistry<BiomeBase> reg = null;

    private static IRegistry<BiomeBase> getBiomeReg() {
    	if (reg == null) {
    		reg = MinecraftServer.getServer().bc().d(Registries.aF);
    	}
    	return reg;
    }

    private Object[] biomelist;
    /**
     * Get list of defined biomebase objects
     */
    @Override
    public Object[] getBiomeBaseList() {
    	if (biomelist == null) {
        	biomelist = new BiomeBase[256];
        	Iterator<BiomeBase> iter = getBiomeReg().iterator();
        	while (iter.hasNext()) {
                BiomeBase b = iter.next();
                int bidx = getBiomeReg().a(b);
        		if (bidx >= biomelist.length) {
        			biomelist = Arrays.copyOf(biomelist, bidx + biomelist.length);
        		}
        		biomelist[bidx] = b;
        	}
        }
        return biomelist;
    }

    /** Get ID from biomebase */
    @Override
    public int getBiomeBaseID(Object bb) {
    	return getBiomeReg().a((BiomeBase)bb);
    }

    public static IdentityHashMap<IBlockData, DynmapBlockState> dataToState;

    /**
     * Initialize block states (org.dynmap.blockstate.DynmapBlockState)
     */
    @Override
    public void initializeBlockStates() {
    	dataToState = new IdentityHashMap<IBlockData, DynmapBlockState>();
    	HashMap<String, DynmapBlockState> lastBlockState = new HashMap<String, DynmapBlockState>();
    	RegistryBlockID<IBlockData> bsids = Block.q;
        Block baseb = null;
    	Iterator<IBlockData> iter = bsids.iterator();
    	ArrayList<String> names = new ArrayList<String>();

    	// Loop through block data states
    	DynmapBlockState.Builder bld = new DynmapBlockState.Builder();
		while (iter.hasNext()) {
    		IBlockData bd = iter.next();
    		Block b = bd.b();
        	MinecraftKey id = BuiltInRegistries.e.b(b);
    		String bname = id.toString();
    		DynmapBlockState lastbs = lastBlockState.get(bname);	// See if we have seen this one
    		int idx = 0;
    		if (lastbs != null) {	// Yes
    			idx = lastbs.getStateCount();	// Get number of states so far, since this is next
    		}
    		// Build state name
    		String sb = "";
    		String fname = bd.toString();
    		int off1 = fname.indexOf('[');
    		if (off1 >= 0) {
    			int off2 = fname.indexOf(']');
    			sb = fname.substring(off1+1, off2);
    		}
            int lightAtten = bd.b(BlockAccessAir.a, BlockPosition.c);	// getLightBlock
            //Log.info("statename=" + bname + "[" + sb + "], lightAtten=" + lightAtten);
            // Fill in base attributes
            bld.setBaseState(lastbs).setStateIndex(idx).setBlockName(bname).setStateName(sb).setAttenuatesLight(lightAtten);
            if (bd.e()) { bld.setSolid(); }
            if (bd.i()) { bld.setAir(); }
            if (bd.a(TagsBlock.t)) { bld.setLog(); }
            if (bd.a(TagsBlock.P)) { bld.setLeaves(); }
            if ((!bd.u().c()) && ((bd.b() instanceof BlockFluids) == false)) {	// Test if fluid type for block is not empty
				bld.setWaterlogged();
				//Log.info("statename=" + bname + "[" + sb + "] = waterlogged");
			}
            DynmapBlockState dbs = bld.build(); // Build state

    		dataToState.put(bd,  dbs);
    		lastBlockState.put(bname, (lastbs == null) ? dbs : lastbs);
    		Log.verboseinfo("blk=" + bname + ", idx=" + idx + ", state=" + sb + ", waterlogged=" + dbs.isWaterlogged());
    	}
    }
    /**
     * Create chunk cache for given chunks of given world
     * @param dw - world
     * @param chunks - chunk list
     * @return cache
     */
    @Override
    public MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks) {
        MapChunkCache121 c = new MapChunkCache121(gencache);
        c.setChunks(dw, chunks);
        return c;
    }
    
	/**
	 * Get biome base water multiplier
	 */
    @Override
	public int getBiomeBaseWaterMult(Object bb) {
    	BiomeBase biome = (BiomeBase) bb;
    	return biome.i();	// waterColor
	}

    /** Get temperature from biomebase */
    @Override
    public float getBiomeBaseTemperature(Object bb) {
    	return ((BiomeBase)bb).g();
    }

    /** Get humidity from biomebase */
    @Override
    public float getBiomeBaseHumidity(Object bb) {
    	String vals = ((BiomeBase)bb).i.toString();	// Sleazy
    	float humidity = 0.5F;
    	int idx = vals.indexOf("downfall=");
    	if (idx >= 0) {
        	humidity = Float.parseFloat(vals.substring(idx+9, vals.indexOf(']', idx)));
    	}
    	return humidity;
    }
    
    @Override
    public Polygon getWorldBorder(World world) {
        Polygon p = null;
        WorldBorder wb = world.getWorldBorder();
        if (wb != null) {
        	Location c = wb.getCenter();
        	double size = wb.getSize();
        	if ((size > 1) && (size < 1E7)) {
        	    size = size / 2;
        		p = new Polygon();
        		p.addVertex(c.getX()-size, c.getZ()-size);
        		p.addVertex(c.getX()+size, c.getZ()-size);
        		p.addVertex(c.getX()+size, c.getZ()+size);
        		p.addVertex(c.getX()-size, c.getZ()+size);
        	}
        }
        return p;
    }
	// Send title/subtitle to user
    public void sendTitleText(Player p, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTIcks) {
    	if (p != null) {
    		p.sendTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTIcks);
    	}
    }

	private String[] biomenames;
	@Override
	public String[] getBiomeNames() {
    	if (biomenames == null) {
        	biomenames = new String[256];
        	Iterator<BiomeBase> iter = getBiomeReg().iterator();
        	while (iter.hasNext()) {
                BiomeBase b = iter.next();
                int bidx = getBiomeReg().a(b);
        		if (bidx >= biomenames.length) {
        			biomenames = Arrays.copyOf(biomenames, bidx + biomenames.length);
        		}
        		biomenames[bidx] = b.toString();
        	}
        }
        return biomenames;
	}

	@Override
    /** Get ID string from biomebase */
    public String getBiomeBaseIDString(Object bb) {
		return getBiomeReg().b((BiomeBase)bb).a();
    }
	@Override
    public String getBiomeBaseResourceLocsation(Object bb) {
        return getBiomeReg().b((BiomeBase)bb).toString();
	}

	@Override
	public Player[] getOnlinePlayers() {
        Collection<? extends Player> p = Bukkit.getServer().getOnlinePlayers();
        return p.toArray(new Player[0]);
	}

    /**
     * Get skin URL for player
     * @param player
     */
	@Override
    public String getSkinURL(Player player) {
		URL url = player.getPlayerProfile().getTextures().getSkin();

		return url != null ? url.toString() : null;
    }
	// Get minY for world
	@Override
	public int getWorldMinY(World w) {
		CraftWorld cw = (CraftWorld) w;
		return cw.getMinHeight();
	}
	@Override
    public boolean useGenericCache() {
    	return true;
    }

}
