package com.iamlordst.realitybending.mixin;

import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies custom arm rotations after vanilla PlayerModel animation has run.
 * The target setupAnim method accepts LivingEntity, not AbstractClientPlayer.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void realitybending$applyPowerPose(LivingEntity player, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        PlayerModel<?> model = (PlayerModel<?>)(Object)this;
        ItemStack using = player.getUseItem();
        if (player.isUsingItem() && using.getItem() instanceof RealityItem item) {
            if (item.getPowerType() == RealityItem.Power.KAMEHAMEHA) {
                // Two hands together, with a small forward lean during the beam charge.
                model.body.xRot = -0.10F;
                model.rightArm.xRot = -1.48F;
                model.leftArm.xRot = -1.48F;
                model.rightArm.yRot = -0.26F;
                model.leftArm.yRot = 0.26F;
                model.rightArm.zRot = -0.12F;
                model.leftArm.zRot = 0.12F;
            } else if (item.getPowerType() == RealityItem.Power.KI_BLAST) {
                // Forward palm with the off-hand pulled back as a brace.
                model.body.xRot = -0.06F;
                model.rightArm.xRot = -1.52F;
                model.leftArm.xRot = -1.12F;
                model.rightArm.yRot = -0.18F;
                model.leftArm.yRot = 0.24F;
                model.rightArm.zRot = -0.08F;
                model.leftArm.zRot = 0.12F;
            } else {
                return;
            }
            model.jacket.copyFrom(model.body);
            model.rightSleeve.copyFrom(model.rightArm);
            model.leftSleeve.copyFrom(model.leftArm);
            return;
        }

        boolean glove = player.getMainHandItem().is(ModItems.TELEKINETIC_GLOVE.get())
                || player.getOffhandItem().is(ModItems.TELEKINETIC_GLOVE.get());
        if (glove && !player.isUsingItem()) {
            // Raised, separated palms for telekinesis.
            model.rightArm.xRot = -1.08F;
            model.leftArm.xRot = -1.08F;
            model.rightArm.yRot = -0.32F;
            model.leftArm.yRot = 0.32F;
            model.rightArm.zRot = -0.42F;
            model.leftArm.zRot = 0.42F;
            model.jacket.copyFrom(model.body);
            model.rightSleeve.copyFrom(model.rightArm);
            model.leftSleeve.copyFrom(model.leftArm);
        }
    }
}
