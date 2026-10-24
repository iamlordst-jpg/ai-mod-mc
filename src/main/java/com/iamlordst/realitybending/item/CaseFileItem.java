package com.iamlordst.realitybending.item;

import com.iamlordst.realitybending.client.ArchiveFileModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/** Animated archive dossier. Historical case-file items intentionally grant no abilities. */
public final class CaseFileItem extends Item implements GeoItem {
    private final String heading;
    private final String context;
    private final String note;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CaseFileItem(String heading, String context, String note, Properties properties) {
        super(properties);
        this.heading = heading;
        this.context = context;
        this.note = note;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "archive_idle", 0,
            state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.archive_file.idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<CaseFileItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new GeoItemRenderer<>(new ArchiveFileModel());
                return renderer;
            }
        });
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
