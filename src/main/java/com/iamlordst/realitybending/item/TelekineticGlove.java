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
            boolean enabled = !stack.getOrCreateTag().getBoolean("TelekinesisEnabled");
            stack.getOrCreateTag().putBoolean("TelekinesisEnabled", enabled);
            player.displayClientMessage(Component.literal("Telekinesis " + (enabled ? "ENABLED" : "DISABLED")
                + " — hold this glove and aim at an entity."), true);
            level.playSound(null, player.blockPosition(), enabled
                ? net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE
                : net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.2F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
