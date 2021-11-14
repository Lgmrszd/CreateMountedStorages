package com.juh9870.moremountedstorages.integrations.industrialforegoing;

import com.buuz135.industrial.block.transportstorage.tile.BlackHoleUnitTile;
import com.buuz135.industrial.utils.Reference;
import com.juh9870.moremountedstorages.Config;
import com.juh9870.moremountedstorages.ContraptionItemStackHandler;
import com.juh9870.moremountedstorages.ContraptionStorageRegistry;
import com.juh9870.moremountedstorages.Utils;
import com.juh9870.moremountedstorages.helpers.AdvancedItemStackHandler;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class IndustrialForegoingRegistry extends ContraptionStorageRegistry {

	public static final Lazy<ContraptionStorageRegistry> INSTANCE = getInstance(Utils.constructId("industrialforegoing", "black_hole_unit"));
	private static final Lazy<TileEntityType<?>[]> affectedStorages = Lazy.of(() -> {
		List<TileEntityType<?>> values = new ArrayList<>();

		for (Rarity rarity : Rarity.values()) {
			TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(Reference.MOD_ID, rarity.name().toLowerCase() + "_black_hole_unit"));
			if (type != null) values.add(type);
		}

		return values.toArray(new TileEntityType<?>[0]);
	});


	@Override
	public Priority getPriority() {
		return Priority.ADDON;
	}

	@Override
	public boolean canUseAsStorage(TileEntity te) {
		return super.canUseAsStorage(te) && Config.INDUSTRIAL_FOREGOING_UNIT.get();
	}

	@Override
	public TileEntityType<?>[] affectedStorages() {
		return affectedStorages.get();
	}

	@Override
	public ContraptionItemStackHandler createHandler(TileEntity te) {
		BlackHoleUnitTile bhu = (BlackHoleUnitTile) te;
		IItemHandler bhHandler = getHandlerFromDefaultCapability(bhu);
		if (bhHandler == dummyHandler) {
			return null;
		}
		return new BlackHoleItemStackHandler(bhHandler).setVoiding(bhu.isVoidItems());
	}

	@Override
	public ContraptionItemStackHandler deserializeHandler(CompoundNBT nbt) {
		return deserializeHandler(new BlackHoleItemStackHandler(), nbt);
	}

	public static class BlackHoleItemStackHandler extends AdvancedItemStackHandler {
		public BlackHoleItemStackHandler() {
			setIgnoreItemStackSize(true);
			setVoiding(true);
		}

		public BlackHoleItemStackHandler(IItemHandler handler) {
			super(handler);
			setIgnoreItemStackSize(true);
			setVoiding(true);
		}

		@Override
		protected ContraptionStorageRegistry registry() {
			return INSTANCE.get();
		}

		@Override
		public boolean addStorageToWorld(TileEntity te) {
			IItemHandler bhHandler = getHandlerFromDefaultCapability(te);
			if (bhHandler == dummyHandler) {
				return false;
			}

			simpleOverwrite(bhHandler);
			return false;
		}
	}
}
