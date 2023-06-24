package com.juh9870.moremountedstorages.mixin;

import com.juh9870.moremountedstorages.ContraptionItemStackHandler;
import com.juh9870.moremountedstorages.ContraptionStorageRegistry;
import com.juh9870.moremountedstorages.MoreMountedStorages;
import com.juh9870.moremountedstorages.helpers.IStorageExposer;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.MountedStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Contraption.class)
public class ContraptionMixin {
	@Shadow(remap = false)
	protected MountedStorageManager storage;

	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V", ordinal = 4), method = "readNBT", remap = false)
	public void moremountedstorages__readNBT(Level world, CompoundTag nbt, boolean spawnData, CallbackInfo cbi) {
		MoreMountedStorages.breakpoint();
		IStorageExposer exposedStorage = (IStorageExposer) this.storage;
		for (MountedStorage value : exposedStorage.getStorage().values()) {
			IItemHandlerModifiable handler = value.getItemHandler();
			if (handler instanceof ContraptionItemStackHandler) {
				((ContraptionItemStackHandler) handler).applyWorld(world);
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "onEntityCreated", remap = false)
	public void moremountedstorages__onEntityCreated(AbstractContraptionEntity face, CallbackInfo ci) {
		IStorageExposer exposedStorage = (IStorageExposer) this.storage;
		// Gather item handlers of mounted storage
		List<IItemHandlerModifiable> list = exposedStorage.getStorage().values()
				.stream()
				.map(MountedStorage::getItemHandler).sorted((a, b) -> {
					int priorityA = 0;
					int priorityB = 0;
					if (a instanceof ContraptionItemStackHandler)
						priorityA = ((ContraptionItemStackHandler) a).getPriority();
					if (b instanceof ContraptionItemStackHandler)
						priorityB = ((ContraptionItemStackHandler) b).getPriority();
					return -(priorityA - priorityB);
				}).collect(Collectors.toList());
		exposedStorage.setInventory(
				new Contraption.ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class))
		);
	}

	@Inject(at = @At(
				value = "INVOKE",
				target = "Lcom/simibubi/create/content/contraptions/components/structureMovement/Contraption;addBlock(Lnet/minecraft/core/BlockPos;Lorg/apache/commons/lang3/tuple/Pair;)V"
			),
			method = "moveBlock",
			remap = false,
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true)
	public void moremountedstorages__moveBlock(Level world, Direction forcedDirection, Queue<BlockPos> frontier, Set<BlockPos> visited,
											   CallbackInfoReturnable<Boolean> cir, BlockPos pos, BlockState state)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if (te == null) {
			return;
		}
		ContraptionStorageRegistry registry = ContraptionStorageRegistry.forBlockEntity(te.getType());
		boolean canBeMoved = true;
		if (registry != null) {
			canBeMoved = registry.moveBlock(world, forcedDirection, frontier, visited, pos, state);
		}
		if (!canBeMoved) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
