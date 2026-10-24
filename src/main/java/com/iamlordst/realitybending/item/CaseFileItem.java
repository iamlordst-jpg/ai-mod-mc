package com.iamlordst.realitybending.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CaseFileItem extends Item {
    private final String heading;
    private final String context;
    private final String note;

    public CaseFileItem(String heading, String context, String note, Properties properties) {
        super(properties);
        this.heading = heading;
        this.context = context;
        this.note = note;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("ARCHIVE / DARK SATIRE").withStyle(style -> style.withColor(0x9B83D7)));
        tooltip.add(Component.literal(context).withStyle(style -> style.withColor(0xC8C8C8)));
        tooltip.add(Component.literal(note).withStyle(style -> style.withItalic(true).withColor(0xAAAAAA)));
        tooltip.add(Component.literal("Use to read the archive entry").withStyle(style -> style.withColor(0x777777)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§5ARCHIVE ENTRY — " + heading), false);
            player.displayClientMessage(Component.literal(context), false);
            player.displayClientMessage(Component.literal(note), false);
            player.displayClientMessage(Component.literal("This item grants no powers. The satire targets the mythology of infamy, not victims."), false);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 0.85F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
