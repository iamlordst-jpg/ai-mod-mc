package com.iamlordst.realitybending.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class TelekineticGlove extends Item {
    public TelekineticGlove(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.getPersistentData().putBoolean("realitybending_telekinesis_unlocked", true);
            player.getPersistentData().putBoolean("realitybending_telekinesis_enabled", false);
            player.displayClientMessage(Component.literal("TELEKINESIS UNLOCKED! Press G to enable it. You no longer need to hold this item."), true);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.25F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
