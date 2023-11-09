package com.juh9870.moremountedstorages.integrations.expandedstorage;

import com.juh9870.moremountedstorages.Config;
import com.juh9870.moremountedstorages.ContraptionItemStackHandler;
import com.juh9870.moremountedstorages.ContraptionStorageRegistry;
import com.juh9870.moremountedstorages.Utils;
import com.juh9870.moremountedstorages.helpers.DoubleChestItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import ellemes.expandedstorage.common.CommonMain;
import ninjaphenix.expandedstorage.api.EsChestType;
import ninjaphenix.expandedstorage.api.ExpandedStorageAccessors;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class ExpandedStorageRegistry extends ContraptionStorageRegistry {

    public static final Config.RegistryInfo CONFIG = new Config.RegistryInfo("storages", "Expanded Storage");
    public static Lazy<ContraptionStorageRegistry> INSTANCE = getInstance(Utils.constructId("expandedstorage", "chest"));


    @Override
    public ContraptionItemStackHandler createHandler(BlockEntity te) {
        Optional<EsChestType> type = ExpandedStorageAccessors.getChestType(te.getBlockState());
        if (type.isPresent() && type.get() != EsChestType.SINGLE) {
            boolean second = true;
            EsChestType esChestType = type.get();
            if (esChestType == EsChestType.LEFT || esChestType == EsChestType.TOP || esChestType == EsChestType.FRONT) {
                second = false;
            }
            return new EsItemStackHandler(getHandlerFromDefaultCapability(te), second, esChestType);
        }
        return super.createHandler(te);
    }

    @Override
    public ContraptionItemStackHandler deserializeHandler(CompoundTag nbt) {
        return deserializeHandler(new EsItemStackHandler(), nbt);
    }

    @Override
    public Priority getPriority() {
        return Priority.ADDON;
    }

    @Override
    public boolean canUseAsStorage(BlockEntity te) {
        return super.canUseAsStorage(te) && CONFIG.isEnabled();
    }

    @Override
    public BlockEntityType<?>[] affectedStorages() {
        return new BlockEntityType<?>[]{
                CommonMain.getBarrelBlockEntityType(),
                CommonMain.getChestBlockEntityType(),
                CommonMain.getOldChestBlockEntityType(),
                CommonMain.getMiniStorageBlockEntityType(),
        };
    }

    @Override
    public boolean moveBlock(Level world,
                             Direction forcedDirection,
                             Queue<BlockPos> frontier,
                             Set<BlockPos> visited,
                             BlockPos pos,
                             BlockState state) {
        // Double Chest halves stick together
        Direction offset = ExpandedStorageAccessors.getAttachedChestDirection(state).orElse(null);
        if (offset != null) {
            BlockPos attached = pos.relative(offset);
            if (!visited.contains(attached))
                frontier.add(attached);
        }
        return true;
    }

    public static class EsItemStackHandler extends DoubleChestItemStackHandler<EsChestType> {

        EsChestType type;

        public EsItemStackHandler() {
        }

        public EsItemStackHandler(IItemHandler chest, boolean second, EsChestType type) {
            super(chest, second);
            this.type = type;
        }

        @Override
        protected BlockPos secondHalfPos(BlockPos pos, BlockState state, EsChestType type) {
            Direction attached = ExpandedStorageAccessors.getAttachedChestDirection(
                    ExpandedStorageAccessors.chestWithType(state, type).orElseThrow(this::throwInvalidBlockState)
            ).orElseThrow(this::throwInvalidBlockState);
            return pos.relative(attached);
        }

        private EsChestType opposite(EsChestType type) {
            switch (type) {
                case LEFT:
                    return EsChestType.RIGHT;
                case RIGHT:
                    return EsChestType.LEFT;
                case TOP:
                    return EsChestType.BOTTOM;
                case BOTTOM:
                    return EsChestType.TOP;
                case FRONT:
                    return EsChestType.BACK;
                case BACK:
                    return EsChestType.FRONT;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        protected void attachToOther(Level level, BlockState self, BlockPos selfPos, BlockState other, BlockPos otherPos) {
            level.setBlockAndUpdate(
                    selfPos,
                    ExpandedStorageAccessors.chestWithType(self, type).orElseThrow(this::throwInvalidBlockState)
            );
            level.setBlockAndUpdate(
                    otherPos,
                    ExpandedStorageAccessors.chestWithType(other, opposite(type)).orElseThrow(this::throwInvalidBlockState)
            );
            level.getBlockEntity(selfPos).setChanged();
            level.getBlockEntity(otherPos).setChanged();
        }

        @Override
        protected boolean isSingle(BlockState state) {
            return ExpandedStorageAccessors.getChestType(state)
                    .orElseThrow(this::throwInvalidBlockState) == EsChestType.SINGLE;
        }

        @Override
        protected EsChestType ownType() {
            return type;
        }

        @Override
        protected EsChestType getType(BlockState state) {
            return ExpandedStorageAccessors.getChestType(state).orElseThrow(this::throwInvalidBlockState);
        }

        @Override
        protected void setSingle(Level level, BlockState state, BlockPos pos) {
            level.setBlockAndUpdate(
                    pos,
                    ExpandedStorageAccessors.chestWithType(state, EsChestType.SINGLE).orElseThrow(this::throwInvalidBlockState)
            );
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        protected ContraptionStorageRegistry registry() {
            return INSTANCE.get();
        }

        private RuntimeException throwInvalidBlockState() {
            return new RuntimeException("Expected values are missing from block state");
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = super.serializeNBT();
            nbt.putString("chestType", type.getSerializedName());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);
            String name = nbt.getString("chestType");
            for (EsChestType value : EsChestType.values()) {
                if (!value.getSerializedName().equals(name)) continue;
                type = value;
                return;
            }
        }
    }
}
