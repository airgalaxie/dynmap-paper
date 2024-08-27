package org.dynmap.bukkit.helper;

import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.dynmap.DynmapChunk;
import org.dynmap.common.chunk.GenericChunkCache;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.Polygon;

/**
 * Helper for isolation of bukkit version specific issues
 */
public abstract class BukkitVersionHelper {
    public static BukkitVersionHelper helper = null;
    
    public static GenericChunkCache gencache;

    protected BukkitVersionHelper() {
        
    }
    /**
     * Get if it's unsafe to load chunks async
     */
    public abstract boolean isUnsafeAsync();
    /**
     * Get list of defined biomebase objects
     */
    public abstract Object[] getBiomeBaseList();
    /** 
     * Get temperature from biomebase
     */
    public abstract float getBiomeBaseTemperature(Object bb);
    /** 
     * Get humidity from biomebase
     */
    public abstract float getBiomeBaseHumidity(Object bb);
    /**
     * Get ID string from biomebase
     */
    public abstract String getBiomeBaseIDString(Object bb);
    /**
     * Get resource location from biomebase (1.18+)
     */
    public abstract String getBiomeBaseResourceLocsation(Object bb);
    /** 
     * Get ID from biomebase
     */
    public abstract int getBiomeBaseID(Object bb);
    /**
     *  Get unload queue for given NMS world
     */
    public abstract Object getUnloadQueue(World world);
    /**
     *  For testing unload queue for presence of givne chunk
     */
    public abstract boolean isInUnloadQueue(Object unloadqueue, int x, int z);
    /**
     * Get inhabited ticks count from chunk
     */
    public abstract long getInhabitedTicks(Chunk c);
    /** 
     * Get tile entities map from chunk
     */
    public abstract Map<?, ?> getTileEntitiesForChunk(Chunk c);
    /**
     * Get X coordinate of tile entity
     */
    public abstract int getTileEntityX(Object te);
    /**
     * Get Y coordinate of tile entity
     */
    public abstract int getTileEntityY(Object te);
    /**
     * Get Z coordinate of tile entity
     */
    public abstract int getTileEntityZ(Object te);
    /**
     * Read tile entity NBT
     */
    public abstract Object readTileEntityNBT(Object te, World world);
    /**
     * Get field value from NBT compound
     */
    public abstract Object getFieldValue(Object nbt, String field);
    /**
     * Unload chunk no save needed
     */
    public abstract void unloadChunkNoSave(World w, Chunk c, int cx, int cz);
    /**
     * Get biome name list
     */
    public abstract String[] getBiomeNames();
    /**
     * Get list of online players
     */
    public abstract Player[] getOnlinePlayers();
    /**
     * Get world border
     */
    public abstract Polygon getWorldBorder(World world);
    /**
     * Get world minY
     */
    public abstract int getWorldMinY(World world);
    /**
     * Test if broken unloadChunk
     */
    public boolean isUnloadChunkBroken() { return false; }
    /**
     * Get skin URL for player
     * @param player
     */
    public abstract String getSkinURL(Player player);
    /**
     * Get material map by block ID
     */
    public abstract BukkitMaterial[] getMaterialList();
    /**
     * Initialize block states (org.dynmap.blockstate.DynmapBlockState)
     */
    public abstract void initializeBlockStates();
    /**
     * Create chunk cache for given chunks of given world
     * @param dw - world
     * @param chunks - chunk list
     * @return cache
     */
    public abstract MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks);
	/**
	 * Get biome base water multiplier
	 */
	public abstract int getBiomeBaseWaterMult(Object bb);
	// Send title/subtitle to user
    public abstract void sendTitleText(Player p, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTIcks);

    public abstract boolean useGenericCache();
}