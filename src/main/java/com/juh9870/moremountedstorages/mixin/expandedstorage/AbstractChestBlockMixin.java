package com.juh9870.moremountedstorages.mixin.expandedstorage;

import ellemes.expandedstorage.common.block.OpenableBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractChestBlock.class)
public class AbstractChestBlockMixin {
    @Redirect(
            at = @At(
                value = "INVOKE",
                target = "Lellemes/expandedstorage/common/block/OpenableBlock;rotate(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/Rotation;)Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            method = "rotate",
            remap = false
    )
    public BlockState rotate(OpenableBlock instance, BlockState state, Rotation rotation) {
        // fix its rotation behavior when the block is in double chest state
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }
}
