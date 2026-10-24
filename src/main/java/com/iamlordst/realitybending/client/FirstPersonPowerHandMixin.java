package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.item.TelekineticGlove;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person power casting poses. These transform the actual rendered player
 * arm, rather than only changing the third-person PlayerModel.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonPowerHandMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void realitybending$beginPowerHandPose(AbstractClientPlayer player, float partialTick,
            float pitch, InteractionHand hand, float swingProgress, ItemStack stack,
            float equipProgress, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, CallbackInfo ci) {
        poseStack.pushPose();

        boolean right = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT
                : player.getMainArm() != net.minecraft.world.entity.HumanoidArm.RIGHT;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack powerStack = stack;
        if (!(powerStack.getItem() instanceof RealityItem)
                && main.getItem() instanceof RealityItem) {
            powerStack = main;
        }

        float t = player.tickCount + partialTick;
        float pulse = (float)Math.sin(t * 0.32F);
        float charge = Math.min(1.0F, player.isUsingItem()
                ? (player.getUseItemRemainingTicks() > 0
                    ? (72000 - player.getUseItemRemainingTicks()) / 45.0F : 0.0F)
                : 0.0F);

        if (powerStack.getItem() instanceof RealityItem item) {
            if (item.getPowerType() == RealityItem.Power.KAMEHAMEHA) {
                // Both forearms draw inward to form the classic two-hand charge.
                poseStack.translate(right ? -0.13F : 0.13F, 0.02F + charge * 0.035F,
                        -0.12F - charge * 0.12F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F - charge * 18.0F + pulse * 1.5F));
                poseStack.mulPose(Axis.YP.rotationDegrees((right ? 1.0F : -1.0F) * (18.0F + charge * 22.0F)));
                poseStack.mulPose(Axis.ZP.rotationDegrees((right ? -1.0F : 1.0F) * (12.0F + charge * 10.0F)));
                poseStack.scale(1.0F + charge * 0.035F, 1.0F + charge * 0.035F, 1.0F + charge * 0.035F);
            } else if (item.getPowerType() == RealityItem.Power.KI_BLAST) {
                // Firing hand thrusts forward; off-hand stays closer as a brace.
                float forward = right ? 1.0F : -0.35F;
                poseStack.translate(right ? -0.08F : 0.10F, 0.01F + pulse * 0.008F,
                        -0.10F - charge * 0.07F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F - charge * 10.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(forward * 12.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(right ? -7.0F : 9.0F));
            } else {
                // Short, readable casting snap for instant-use powers.
                float cast = Math.max(0.0F, 1.0F - swingProgress);
                poseStack.translate(right ? -0.04F : 0.04F, -cast * 0.035F, -cast * 0.11F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-cast * 16.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees((right ? -1.0F : 1.0F) * cast * 9.0F));
            }
        } else if (TelekinesisKeybind.isLocallyEnabled()
                && (stack.isEmpty() || stack.getItem() instanceof TelekineticGlove)) {
            // Telekinesis holds the empty palm out, fingers angled upward.
            poseStack.translate(right ? -0.10F : 0.10F, 0.035F + pulse * 0.008F, -0.16F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-23.0F + pulse * 2.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(right ? 12.0F : -12.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(right ? -16.0F : 16.0F));
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void realitybending$endPowerHandPose(AbstractClientPlayer player, float partialTick,
            float pitch, InteractionHand hand, float swingProgress, ItemStack stack,
            float equipProgress, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, CallbackInfo ci) {
        poseStack.popPose();
    }
}
