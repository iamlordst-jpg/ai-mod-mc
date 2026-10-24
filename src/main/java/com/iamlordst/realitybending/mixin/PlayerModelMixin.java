package com.iamlordst.realitybending.mixin;

import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies custom arm rotations after vanilla PlayerModel animation has run.
 * RenderPlayerEvent.Pre runs too early for reliable direct model rotations.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Shadow public net.minecraft.client.model.geom.ModelPart rightArm;
    @Shadow public net.minecraft.client.model.geom.ModelPart leftArm;
    @Shadow public net.minecraft.client.model.geom.ModelPart rightSleeve;
    @Shadow public net.minecraft.client.model.geom.ModelPart leftSleeve;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void realitybending$applyPowerPose(AbstractClientPlayer player, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack using = player.getUseItem();
        if (player.isUsingItem() && using.getItem() instanceof RealityItem item) {
            if (item.getPowerType() == RealityItem.Power.KAMEHAMEHA) {
                // Both hands together in front of the chest, ready to push the beam forward.
                rightArm.xRot = -1.48F;
                leftArm.xRot = -1.48F;
                rightArm.yRot = -0.26F;
                leftArm.yRot = 0.26F;
                rightArm.zRot = -0.12F;
                leftArm.zRot = 0.12F;
            } else if (item.getPowerType() == RealityItem.Power.KI_BLAST) {
                // Forward palm with the off-hand pulled back as a brace.
                rightArm.xRot = -1.52F;
                leftArm.xRot = -1.12F;
                rightArm.yRot = -0.18F;
                leftArm.yRot = 0.24F;
                rightArm.zRot = -0.08F;
                leftArm.zRot = 0.12F;
            } else {
                return;
            }
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            return;
        }

        boolean glove = player.getMainHandItem().is(ModItems.TELEKINETIC_GLOVE.get())
                || player.getOffhandItem().is(ModItems.TELEKINETIC_GLOVE.get());
        if (glove && !player.isUsingItem()) {
            // Raised, separated palms for telekinesis.
            rightArm.xRot = -1.08F;
            leftArm.xRot = -1.08F;
            rightArm.yRot = -0.32F;
            leftArm.yRot = 0.32F;
            rightArm.zRot = -0.42F;
            leftArm.zRot = 0.42F;
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
        }
    }
}
