package com.juh9870.moremountedstorages.mixin;

import com.juh9870.moremountedstorages.ContraptionItemStackHandler;
import com.juh9870.moremountedstorages.ContraptionStorageRegistry;
import com.simibubi.create.content.contraptions.MountedStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MountedStorage.class)
public class MountedStorageMixin {

	@Shadow(remap = false)
	@Final
	private static ItemStackHandler dummyHandler;
	@Shadow(remap = false)
	ItemStackHandler handler;
	@Shadow(remap = false)
	boolean valid;
	@Shadow(remap = false)
	private BlockEntity blockEntity;

	public MountedStorageMixin(BlockEntity te) {
	}

	/**
	 * @author juh9870
	 */
	// @Inject(at = @At(value = "TAIL"), method = "canUseAsStorage", remap = false, cancellable=true)
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"),
			method = "canUseAsStorage", remap = false, cancellable=true)
	private static void moremountedstorages__canUseAsStorage(BlockEntity te, CallbackInfoReturnable<Boolean> cir) {
		ContraptionStorageRegistry registry = ContraptionStorageRegistry.forBlockEntity(te.getType());
		if (registry != null) {
			cir.setReturnValue(registry.canUseAsStorage(te));
			cir.cancel();
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "deserialize(Lnet/minecraft/nbt/CompoundTag;)Lcom/simibubi/create/content/contraptions/MountedStorage;", remap = false, cancellable = true)
	private static void moremountedstorages_deserializeMountedStorage(CompoundTag nbt, CallbackInfoReturnable<MountedStorageMixin> cir) {
		MountedStorageMixin storage = new MountedStorageMixin(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null){
			cir.setReturnValue(storage);
			return;
		}

		if (nbt.contains(ContraptionStorageRegistry.REGISTRY_NAME)) {
			String id = nbt.getString(ContraptionStorageRegistry.REGISTRY_NAME);
			ContraptionStorageRegistry registry = ContraptionStorageRegistry.REGISTRY.get().getValue(new ResourceLocation(id));
			if (registry != null) {
				storage.handler = registry.deserializeHandler(nbt);
				if (storage.handler == null) {
					storage.handler = dummyHandler;
				} else {
					storage.valid = true;
				}
				cir.setReturnValue(storage);
			}
		}

		storage.valid = true;

		storage.handler.deserializeNBT(nbt);
		cir.setReturnValue(storage);
	}

	/**
	 * @author juh9870
	 */
	//@Overwrite(remap = false)
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"),
			method = "removeStorageFromWorld", remap = false, cancellable = true)
	public void moremountedstorages__removeStorageFromWorld(CallbackInfo ci) {
		ContraptionStorageRegistry registry = ContraptionStorageRegistry.forBlockEntity(blockEntity.getType());
		if (registry == null) return;
		IItemHandler teHandler = registry.createHandler(blockEntity);
		if (teHandler != null) {
			handler = (ContraptionItemStackHandler) teHandler;
			valid = true;
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "addStorageToWorld(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", remap = false, cancellable = true)
	private void moremountedstorages_addStorageToWorld(BlockEntity te, CallbackInfo ci) {
		if (handler instanceof ContraptionItemStackHandler) {
			boolean cancel = !((ContraptionItemStackHandler) handler).addStorageToWorld(te);
			if (cancel) {
				ci.cancel();
			}
		}
	}
}
