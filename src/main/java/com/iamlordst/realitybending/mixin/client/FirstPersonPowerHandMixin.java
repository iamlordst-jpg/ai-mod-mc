package com.iamlordst.realitybending.mixin.client;

import com.iamlordst.realitybending.client.PowerAbilityKeybind;
import com.iamlordst.realitybending.client.TelekinesisKeybind;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonPowerHandMixin {
    @Shadow private void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffers, int light, float equipProgress, float swingProgress, HumanoidArm arm) { throw new AssertionError(); }

    // Force both skin-textured arms to render while telekinesis is active, even if an item is held.
    @Inject(method="renderHandsWithItems", at=@At("HEAD"), cancellable=true)
    private void realitybending$forceTelekinesisHands(float partialTick, PoseStack poseStack, BufferSource buffers, LocalPlayer player, int packedLight, CallbackInfo ci) {
        if (!TelekinesisKeybind.isLocallyEnabled()) return;
        poseStack.pushPose();
        float swing = player.getAttackAnim(partialTick);
        renderPlayerArm(poseStack, buffers, packedLight, 0.0F, swing, HumanoidArm.RIGHT);
        renderPlayerArm(poseStack, buffers, packedLight, 0.0F, swing, HumanoidArm.LEFT);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique private boolean realitybending$posePushed=false;
    @Inject(method="renderPlayerArm",at=@At("HEAD"))
    private void realitybending$beginArmPose(PoseStack poseStack,MultiBufferSource buffers,int light,float equip,float swing,HumanoidArm side,CallbackInfo ci){
        realitybending$posePushed=false;Player player=Minecraft.getInstance().player;if(player==null)return;
        int ability=-1;for(int i=0;i<6;i++)if(PowerAbilityKeybind.isCharging(i)){ability=i;break;}
        boolean tele=TelekinesisKeybind.isLocallyEnabled();
        if(ability<0&&!tele)return;
        poseStack.pushPose();realitybending$posePushed=true;
        boolean right=side==HumanoidArm.RIGHT;float time=player.tickCount+Minecraft.getInstance().getFrameTime();float pulse=(float)Math.sin(time*.32F);
        if(tele&&ability<0){
            // Eleven-inspired pose: raised, open palm reaching toward the crosshair.
            poseStack.translate(right?-.035F:.035F,.72F+pulse*.018F,-.24F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-56F+pulse*1.2F));
            poseStack.mulPose(Axis.YP.rotationDegrees(right?8F:-8F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(right?-13F:13F));
        } else if(ability==1){
            poseStack.translate(right?-.11F:.11F,.22F+pulse*.014F,-.10F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-10F+pulse));
            poseStack.mulPose(Axis.YP.rotationDegrees((right?1:-1)*13F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((right?-1:1)*8F));
        } else if(ability==0){
            poseStack.translate(right?-.12F:.08F,.20F+pulse*.014F,-.12F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12F+pulse*1.5F));
            poseStack.mulPose(Axis.YP.rotationDegrees(right?12F:-8F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(right?-10F:8F));
        } else if(ability==2){
            poseStack.translate(right?-.13F:.13F,.22F+pulse*.016F,-.10F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-16F+pulse));
            poseStack.mulPose(Axis.YP.rotationDegrees((right?1:-1)*18F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((right?-1:1)*17F));
        } else {
            poseStack.translate(right?-.11F:.11F,.19F+pulse*.012F,-.08F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12F+pulse));
            poseStack.mulPose(Axis.YP.rotationDegrees((right?1:-1)*12F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((right?-1:1)*12F));
        }
    }
    @Inject(method="renderPlayerArm",at=@At("RETURN"))
    private void realitybending$endArmPose(PoseStack poseStack,MultiBufferSource buffers,int light,float equip,float swing,HumanoidArm side,CallbackInfo ci){
        if(realitybending$posePushed){poseStack.popPose();realitybending$posePushed=false;}
    }
}