package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.entity.EnergyProjectile;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class EnergyProjectileRenderer extends EntityRenderer<EnergyProjectile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RealityBending.MOD_ID, "textures/entity/ki_energy.png");
    public EnergyProjectileRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }

    @Override public void render(EnergyProjectile e, float yaw, float partial, PoseStack stack, MultiBufferSource buffers, int light) {
        stack.pushPose();
        stack.translate(0, e.getBbHeight() * 0.5F, 0);
        VertexConsumer out = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        PoseStack.Pose pose = stack.last();
        if (e.visualType() == 1) {
            net.minecraft.world.phys.Vec3 d = e.getDeltaMovement().normalize();
            stack.mulPose(Axis.YP.rotation((float)Math.atan2(-d.x, d.z)));
            stack.mulPose(Axis.XP.rotation((float)Math.atan2(-d.y, Math.sqrt(d.x*d.x + d.z*d.z))));
            stack.mulPose(Axis.ZP.rotation((e.tickCount + partial) * 0.035F));
            pose = stack.last();
            drawTube(out, pose, 0.95F, e.beamLength(), 0.28F, 0.63F, 1.0F, 0.75F);
            drawTube(out, pose, 0.61F, e.beamLength(), 0.62F, 0.86F, 1.0F, 0.92F);
            drawTube(out, pose, 0.28F, e.beamLength(), 0.95F, 0.99F, 1.0F, 1.0F);
        } else {
            float charge = e.visualType() == 2 ? e.charge() : 0.35F;
            float pulse = 1.0F + Mth.sin((e.tickCount + partial) * 0.38F) * 0.055F;
            float radius = (e.visualType() == 2 ? 0.28F + charge * 0.58F : 0.42F) * pulse;
            drawSphere(out, pose, radius, 0.20F, 0.60F, 1.0F, 0.65F);
            drawSphere(out, pose, radius * 0.70F, 0.58F, 0.85F, 1.0F, 0.92F);
            drawSphere(out, pose, radius * 0.38F, 0.92F, 0.98F, 1.0F, 1.0F);
        }
        stack.popPose();
        super.render(e, yaw, partial, stack, buffers, light);
    }

    private static void drawSphere(VertexConsumer out, PoseStack.Pose p, float r, float cr, float cg, float cb, float a) {
        int slices=24, stacks=16;
        for(int y=0;y<stacks;y++) for(int x=0;x<slices;x++) {
            double v0=Math.PI*y/stacks, v1=Math.PI*(y+1)/stacks, u0=2*Math.PI*x/slices, u1=2*Math.PI*(x+1)/slices;
            vertex(out,p,r,v0,u0,(float)x/slices,(float)y/stacks,cr,cg,cb,a);
            vertex(out,p,r,v1,u0,(float)x/slices,(float)(y+1)/stacks,cr,cg,cb,a);
            vertex(out,p,r,v1,u1,(float)(x+1)/slices,(float)(y+1)/stacks,cr,cg,cb,a);
            vertex(out,p,r,v0,u1,(float)(x+1)/slices,(float)y/stacks,cr,cg,cb,a);
        }
    }
    private static void vertex(VertexConsumer o, PoseStack.Pose p, float r, double v, double u, float tu, float tv, float cr, float cg, float cb, float a) {
        float x=(float)(r*Math.sin(v)*Math.cos(u)), y=(float)(r*Math.cos(v)), z=(float)(r*Math.sin(v)*Math.sin(u));
        o.vertex(p.pose(),x,y,z).color(cr,cg,cb,a).uv(tu,tv).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),0,1,0).endVertex();
    }
    private static void drawTube(VertexConsumer o, PoseStack.Pose p, float r, float length, float cr, float cg, float cb, float a) {
        int sides=32;
        for(int i=0;i<sides;i++) {
            double a0=2*Math.PI*i/sides, a1=2*Math.PI*(i+1)/sides;
            float x0=(float)Math.cos(a0)*r,y0=(float)Math.sin(a0)*r,x1=(float)Math.cos(a1)*r,y1=(float)Math.sin(a1)*r;
            o.vertex(p.pose(),x0,y0,0).color(cr,cg,cb,a).uv((float)i/sides,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),x0,y0,0).endVertex();
            o.vertex(p.pose(),x1,y1,0).color(cr,cg,cb,a).uv((float)(i+1)/sides,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),x1,y1,0).endVertex();
            o.vertex(p.pose(),x1,y1,length).color(cr,cg,cb,a).uv((float)(i+1)/sides,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),x1,y1,0).endVertex();
            o.vertex(p.pose(),x0,y0,length).color(cr,cg,cb,a).uv((float)i/sides,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),x0,y0,0).endVertex();
        }
    }
    @Override public ResourceLocation getTextureLocation(EnergyProjectile entity) { return TEXTURE; }
}
