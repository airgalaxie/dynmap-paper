package org.dynmap.hdmap;

import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.PatchDefinition;

public class HDScaledBlockModels {
    private short[][] modelvectors;
    // These are scale invariant - only need once
    private static PatchDefinition[][] patches;
    private static CustomBlockModel[] custom;

    public HDScaledBlockModels(int scale) {
        short[][] blockmodels = new short[DynmapBlockState.getGlobalIndexMax()][];
        PatchDefinition[][] newpatches = null;
        if (patches == null) { 
        	newpatches = new PatchDefinition[DynmapBlockState.getGlobalIndexMax()][];
        	patches = newpatches;
        }
        CustomBlockModel[] newcustom = null;
        if (custom == null) {
        	newcustom = new CustomBlockModel[DynmapBlockState.getGlobalIndexMax()];
        	custom = newcustom;
        }
        for(int gidx = 0; gidx < HDBlockModels.models_by_id_data.length; gidx++) {
            HDBlockModel m = HDBlockModels.models_by_id_data[gidx];
            if(m == null) continue;

            if(m instanceof HDBlockVolumetricModel) {
                HDBlockVolumetricModel vm = (HDBlockVolumetricModel)m;
                short[] smod = vm.getScaledMap(scale);
                /* See if scaled model is full block : much faster to not use it if it is */
                if(smod != null) {
                    boolean keep = false;
                    for(int i = 0; (!keep) && (i < smod.length); i++) {
                        if(smod[i] == 0) keep = true;
                    }
                    if(keep) {
                        blockmodels[gidx] = smod;
                    }
                    else {
                        blockmodels[gidx] = null;
                    }
                }
            }
            else if(m instanceof HDBlockPatchModel) {
            	if (newpatches != null) {
            		HDBlockPatchModel pm = (HDBlockPatchModel)m;
            		newpatches[gidx] = pm.getPatches();
            	}
            }
            else if(m instanceof CustomBlockModel) {
            	if (newcustom != null) {
            		CustomBlockModel cbm = (CustomBlockModel)m;
            		newcustom[gidx] = cbm;
            	}
            }
        }
        this.modelvectors = blockmodels;
    }
    
    public final short[] getScaledModel(DynmapBlockState blk) {
        int idx = blk.globalStateIndex;
        if(idx >= modelvectors.length) {
            short[][] newmodels = new short[idx + 1][];
            System.arraycopy(modelvectors, 0, newmodels, 0, modelvectors.length);
            modelvectors = newmodels;
            return null;
        }
        return modelvectors[idx];
    }
    public PatchDefinition[] getPatchModel(DynmapBlockState blk) {
        int idx = blk.globalStateIndex;
        if(idx >= patches.length) {
            PatchDefinition[][] newpatches = new PatchDefinition[idx + 1][];
            System.arraycopy(patches, 0, newpatches, 0, patches.length);
            patches = newpatches;
            return null;
        }
        return patches[idx];
    }

    public CustomBlockModel getCustomBlockModel(DynmapBlockState blk) {
        int idx = blk.globalStateIndex;
        if(idx >= custom.length) {
            CustomBlockModel[] newcustom = new CustomBlockModel[idx + 1];
            System.arraycopy(custom, 0, newcustom, 0, custom.length);
            custom = newcustom;
            return null;
        }
        return custom[idx];
    }
}
