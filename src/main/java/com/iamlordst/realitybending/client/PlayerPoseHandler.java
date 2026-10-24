package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, value = Dist.CLIENT)
public final class PlayerPoseHandler {
    private PlayerPoseHandler() {}

    @SubscribeEvent
    public static void animateArms(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerModel<?> model = event.getRenderer().getModel();
        ItemStack using = player.getUseItem();

        if (player.isUsingItem() && using.getItem() instanceof RealityItem item) {
            if (item.getPowerType() == RealityItem.Power.KAMEHAMEHA) {
                // Force a two-handed, forward-reaching charge pose.
                model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.rightArm.xRot = -1.50F;
                model.leftArm.xRot = -1.50F;
                model.rightArm.yRot = -0.30F;
                model.leftArm.yRot = 0.30F;
                model.rightArm.zRot = -0.10F;
                model.leftArm.zRot = 0.10F;
            } else if (item.getPowerType() == RealityItem.Power.KI_BLAST) {
                // One arm presents the blast; the other braces it.
                model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.rightArm.xRot = -1.50F;
                model.leftArm.xRot = -1.18F;
                model.rightArm.yRot = -0.20F;
                model.leftArm.yRot = 0.22F;
                model.rightArm.zRot = -0.08F;
                model.leftArm.zRot = 0.08F;
            }
        }

        boolean glove = player.getMainHandItem().is(ModItems.TELEKINETIC_GLOVE.get())
            || player.getOffhandItem().is(ModItems.TELEKINETIC_GLOVE.get());
        if (glove && !player.isUsingItem()) {
            // Raised, spread hands for telekinesis instead of the default relaxed arms.
            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            model.rightArm.xRot = -1.05F;
            model.leftArm.xRot = -1.05F;
            model.rightArm.yRot = -0.28F;
            model.leftArm.yRot = 0.28F;
            model.rightArm.zRot = -0.48F;
            model.leftArm.zRot = 0.48F;
        }
    }
}
