package com.juh9870.moremountedstorages.mixin;

import com.juh9870.moremountedstorages.helpers.IStorageExposer;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.MountedStorage;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(MountedStorageManager.class)
public class MountedStorageManagerMixin implements IStorageExposer {
	@Shadow(remap = false)
	protected Contraption.ContraptionInvWrapper inventory;
	@Shadow(remap = false)
	protected Map<BlockPos, MountedStorage> storage;

	@Override
	public Contraption.ContraptionInvWrapper getInventory() {
		return inventory;
	}

	@Override
	public void setInventory(Contraption.ContraptionInvWrapper inv) {
		inventory = inv;
	}

	@Override
	public Map<BlockPos, MountedStorage> getStorage() {
		return storage;
	}
}
