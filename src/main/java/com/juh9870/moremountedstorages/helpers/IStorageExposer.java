package com.juh9870.moremountedstorages.helpers;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorage;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface IStorageExposer {
    Contraption.ContraptionInvWrapper getInventory();
    void setInventory(Contraption.ContraptionInvWrapper inv);

    Map<BlockPos, MountedStorage> getStorage();
}
