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
        if (e.visualType() == 1 || e.visualType() == 4) {
            net.minecraft.world.phys.Vec3 d=e.getDeltaMovement().normalize();
            stack.mulPose(Axis.YP.rotation((float)Math.atan2(-d.x,d.z)));
            stack.mulPose(Axis.XP.rotation((float)Math.atan2(-d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
            stack.mulPose(Axis.ZP.rotation((e.tickCount+partial)*.045F)); pose=stack.last();
            boolean spear=e.visualType()==4;
            drawTube(out,pose,spear?.48F:.98F,spear?2.4F:e.beamLength(),spear?1F:.12F,spear?.88F:.45F,spear?.18F:1F,.92F);
            drawTube(out,pose,spear?.26F:.64F,spear?2.4F:e.beamLength(),1F,.96F,spear?.65F:1F,1F);
            drawTube(out,pose,spear?.10F:.3F,spear?2.4F:e.beamLength(),1F,1F,1F,1F);
        } else if(e.visualType()==3) {
            float t=e.tickCount+partial,pulse=1F+Mth.sin(t*.24F)*.1F;
            drawSphere(out,pose,.78F*pulse,.004F,.001F,.015F,1F);
            drawSphere(out,pose,.45F*pulse,0F,0F,0F,1F);
            stack.mulPose(Axis.YP.rotation(t*.13F));pose=stack.last();
            drawRing(out,pose,1.02F,.12F,.64F,.12F,1F,.98F);
            stack.mulPose(Axis.XP.rotation(.78F));pose=stack.last();
            drawRing(out,pose,.86F,.09F,.28F,.72F,1F,.94F);
            stack.mulPose(Axis.YP.rotation(-t*.19F));pose=stack.last();
            drawRing(out,pose,1.28F,.045F,.9F,.3F,1F,.9F);
        } else {
            float charge=e.visualType()==2?e.charge():1F;
            float pulse=1F+Mth.sin((e.tickCount+partial)*.5F)*.09F;
            float radius=(e.visualType()==2?.4F+charge*.78F:.42F)*pulse;
            drawSphere(out,pose,radius,.12F,.34F,1F,1F);
            drawSphere(out,pose,radius*.78F,.2F,.72F,1F,1F);
            drawSphere(out,pose,radius*.5F,.65F,.94F,1F,1F);
            drawSphere(out,pose,radius*.25F,1F,1F,1F,1F);
            stack.mulPose(Axis.YP.rotation((e.tickCount+partial)*.18F));pose=stack.last();
            drawRing(out,pose,radius*1.3F,.045F,.55F,.86F,1F,1F);
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
    private static void drawRing(VertexConsumer o,PoseStack.Pose p,float radius,float width,float cr,float cg,float cb,float alpha) {
        int n=64;
        for(int i=0;i<n;i++){
            double a0=2*Math.PI*i/n,a1=2*Math.PI*(i+1)/n;
            float x0=(float)Math.cos(a0),z0=(float)Math.sin(a0),x1=(float)Math.cos(a1),z1=(float)Math.sin(a1);
            ringVertex(o,p,x0*(radius-width),z0*(radius-width),cr,cg,cb,alpha,(float)i/n,0);
            ringVertex(o,p,x0*(radius+width),z0*(radius+width),cr,cg,cb,alpha,(float)i/n,1);
            ringVertex(o,p,x1*(radius+width),z1*(radius+width),cr,cg,cb,alpha,(float)(i+1)/n,1);
            ringVertex(o,p,x1*(radius-width),z1*(radius-width),cr,cg,cb,alpha,(float)(i+1)/n,0);
        }
    }
    private static void ringVertex(VertexConsumer o,PoseStack.Pose p,float x,float z,float cr,float cg,float cb,float alpha,float u,float v) {
        o.vertex(p.pose(),x,0,z).color(cr,cg,cb,alpha).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),0,1,0).endVertex();
    }
    @Override public ResourceLocation getTextureLocation(EnergyProjectile entity) { return TEXTURE; }
}
