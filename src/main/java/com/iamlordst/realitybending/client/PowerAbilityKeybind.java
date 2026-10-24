package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.network.ModNetwork;
import com.iamlordst.realitybending.network.PowerAbilityPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PowerAbilityKeybind {
    public static final KeyMapping KI_BLAST = key("key.realitybending.ki_blast_ability", GLFW.GLFW_KEY_V);
    public static final KeyMapping KAMEHAMEHA = key("key.realitybending.kamehameha_ability", GLFW.GLFW_KEY_B);
    public static final KeyMapping BLACK_HOLE = key("key.realitybending.black_hole_ability", GLFW.GLFW_KEY_N);
    public static final KeyMapping GRAVITY_CRUSH = key("key.realitybending.gravity_crush_ability", GLFW.GLFW_KEY_H);
    public static final KeyMapping LIGHTNING_SPEAR = key("key.realitybending.lightning_spear_ability", GLFW.GLFW_KEY_J);
    public static final KeyMapping VOID_PULSE = key("key.realitybending.void_pulse_ability", GLFW.GLFW_KEY_K);
    private static final KeyMapping[] KEYS = {KI_BLAST,KAMEHAMEHA,BLACK_HOLE,GRAVITY_CRUSH,LIGHTNING_SPEAR,VOID_PULSE};
    private static final boolean[] WAS_DOWN = new boolean[KEYS.length];
    private static final boolean[] CHARGING = new boolean[KEYS.length];
    private PowerAbilityKeybind() {}
    private static KeyMapping key(String name,int key) { return new KeyMapping(name,InputConstants.Type.KEYSYM,key,"key.categories.realitybending"); }
    public static boolean isCharging(int ability) { return ability>=0 && ability<CHARGING.length && CHARGING[ability]; }
    @SubscribeEvent public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){java.util.Arrays.fill(WAS_DOWN,false);java.util.Arrays.fill(CHARGING,false);return;}
        for(int i=0;i<KEYS.length;i++){
            boolean down=KEYS[i].isDown();
            if(down&&!WAS_DOWN[i]){
                boolean busy=false;for(boolean active:CHARGING)if(active)busy=true;
                if(!busy){CHARGING[i]=true;ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(i,true));}
            } else if(!down&&WAS_DOWN[i]&&CHARGING[i]){
                CHARGING[i]=false;ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(i,false));
            }
            WAS_DOWN[i]=down;
        }
    }
}
