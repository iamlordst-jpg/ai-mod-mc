package com.iamlordst.realitybending.mixin.client;

import com.iamlordst.realitybending.client.TelekinesisKeybind;
import com.iamlordst.realitybending.client.PowerAbilityKeybind;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.item.TelekineticGlove;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes only the first-person arm render transform. Do not inject into
 * renderArmWithItem: that method also transforms the held item, making the
 * item move while the arm stays effectively unchanged.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonPowerHandMixin {
    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void realitybending$beginArmPose(PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, float equipProgress, float swingProgress, HumanoidArm side,
            CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        poseStack.pushPose();
        boolean right = side == HumanoidArm.RIGHT;
        ItemStack held = right == (player.getMainArm() == HumanoidArm.RIGHT)
                ? player.getMainHandItem() : player.getOffhandItem();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        float time = player.tickCount + Minecraft.getInstance().getFrameTime();
        float pulse = (float)Math.sin(time * 0.30F);

        if (PowerAbilityKeybind.isCharging(1)) {
            float charge = Math.min(1.0F, (player.tickCount % 50) / 50.0F);
            poseStack.translate(right ? -0.15F : 0.15F, 0.025F + charge * 0.035F, -0.13F - charge * 0.14F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-17.0F - charge * 22.0F + pulse * 1.8F));
            poseStack.mulPose(Axis.YP.rotationDegrees((right ? 1 : -1) * (20.0F + charge * 20.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees((right ? -1 : 1) * (13.0F + charge * 12.0F)));
            return;
        }
        if (PowerAbilityKeybind.isCharging(0)) {
            poseStack.translate(right ? -0.10F : 0.08F, pulse * 0.012F, -0.12F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-13.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(right ? 14.0F : -8.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(right ? -9.0F : 8.0F));
            return;
        }

        if (held.getItem() instanceof RealityItem item) {
            boolean charging = player.isUsingItem() && player.getUseItem().is(held.getItem());
            float charge = charging ? Math.min(1.0F,
                    (72000 - player.getUseItemRemainingTicks()) / 48.0F) : 0.0F;
            if (item.getPowerType() == RealityItem.Power.KAMEHAMEHA) {
                poseStack.translate(right ? -0.15F : 0.15F, 0.025F + charge * 0.035F,
                        -0.13F - charge * 0.14F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F - charge * 22.0F + pulse * 1.8F));
                poseStack.mulPose(Axis.YP.rotationDegrees((right ? 1 : -1) * (20.0F + charge * 20.0F)));
                poseStack.mulPose(Axis.ZP.rotationDegrees((right ? -1 : 1) * (13.0F + charge * 12.0F)));
            } else if (item.getPowerType() == RealityItem.Power.KI_BLAST) {
                poseStack.translate(right ? -0.10F : 0.08F, pulse * 0.012F,
                        -0.12F - charge * 0.08F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F - charge * 13.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(right ? 14.0F : -8.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(right ? -9.0F : 8.0F));
            } else {
                float cast = Math.max(0.0F, 1.0F - swingProgress);
                poseStack.translate(right ? -0.04F : 0.04F, -cast * 0.025F, -cast * 0.08F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-cast * 12.0F));
            }
            return;
        }

        boolean hasGlove = main.getItem() instanceof TelekineticGlove
                || off.getItem() instanceof TelekineticGlove;
        if (TelekinesisKeybind.isLocallyEnabled()
                && (held.isEmpty() || held.getItem() instanceof TelekineticGlove || hasGlove)) {
            poseStack.translate(right ? -0.11F : 0.11F, 0.045F + pulse * 0.009F, -0.15F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-27.0F + pulse * 2.2F));
            poseStack.mulPose(Axis.YP.rotationDegrees(right ? 13.0F : -13.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(right ? -18.0F : 18.0F));
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("RETURN"))
    private void realitybending$endArmPose(PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, float equipProgress, float swingProgress, HumanoidArm side,
            CallbackInfo ci) {
        // This method only runs when an arm is being rendered. A balanced pose
        // stack keeps vanilla's item transform and later render calls untouched.
        if (Minecraft.getInstance().player != null) poseStack.popPose();
    }
}
