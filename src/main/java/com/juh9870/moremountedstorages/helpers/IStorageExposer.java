package com.juh9870.moremountedstorages.helpers;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MountedStorage;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface IStorageExposer {
    Contraption.ContraptionInvWrapper getInventory();
    void setInventory(Contraption.ContraptionInvWrapper inv);

    Map<BlockPos, MountedStorage> getStorage();
}
