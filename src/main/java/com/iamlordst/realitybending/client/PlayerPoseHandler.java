package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=RealityBending.MOD_ID,value=Dist.CLIENT)
public final class PlayerPoseHandler {
    private PlayerPoseHandler() {}
    @SubscribeEvent public static void animate(RenderPlayerEvent.Pre event) {
        PlayerModel<?> model=event.getRenderer().getModel();
        float t=event.getEntity().tickCount+event.getPartialTick();
        float pulse=(float)Math.sin(t*.3F);
        int ability=-1;
        for(int i=0;i<6;i++)if(PowerAbilityKeybind.isCharging(i)){ability=i;break;}
        if(ability<0&&!TelekinesisKeybind.isLocallyEnabled())return;
        model.rightArmPose=HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose=HumanoidModel.ArmPose.EMPTY;
        model.rightArm.xRot=-1.18F+pulse*.025F;
        model.leftArm.xRot=-1.18F+pulse*.025F;
        model.rightArm.yRot=ability==1?-.2F:-.28F;
        model.leftArm.yRot=ability==1?.2F:.28F;
        model.rightArm.zRot=ability==2?-.4F:-.35F;
        model.leftArm.zRot=ability==2?.4F:.35F;
    }
}
