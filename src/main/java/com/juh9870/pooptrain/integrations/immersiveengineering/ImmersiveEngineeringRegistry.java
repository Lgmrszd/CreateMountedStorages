package com.juh9870.pooptrain.integrations.immersiveengineering;

import blusunrize.immersiveengineering.common.IETileTypes;
import com.juh9870.pooptrain.ContraptionStorageRegistry;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.IForgeRegistry;

public class ImmersiveEngineeringRegistry extends ContraptionStorageRegistry {
	public static final Lazy<ContraptionStorageRegistry> INSTANCE = createIfModLoaded(
			"immersiveengineering",
			"immersiveengineering:crate",
			ImmersiveEngineeringRegistry::new
	);

	public static void register(IForgeRegistry<ContraptionStorageRegistry> registry) {
		registry.register(INSTANCE.get());
	}

	@Override
	public TileEntityType<?>[] affectedStorages() {
		return new TileEntityType[]{IETileTypes.WOODEN_CRATE.get()};
	}
}
