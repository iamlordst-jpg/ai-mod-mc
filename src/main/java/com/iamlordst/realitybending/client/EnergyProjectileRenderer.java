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
            // Kamehameha: layered blue-white beam with a bright core.
            net.minecraft.world.phys.Vec3 d = e.getDeltaMovement().normalize();
            stack.mulPose(Axis.YP.rotation((float)Math.atan2(-d.x, d.z)));
            stack.mulPose(Axis.XP.rotation((float)Math.atan2(-d.y, Math.sqrt(d.x*d.x + d.z*d.z))));
            stack.mulPose(Axis.ZP.rotation((e.tickCount + partial) * 0.035F));
            pose = stack.last();
            drawTube(out, pose, 0.95F, e.beamLength(), 0.28F, 0.63F, 1.0F, 0.75F);
            drawTube(out, pose, 0.61F, e.beamLength(), 0.62F, 0.86F, 1.0F, 0.92F);
            drawTube(out, pose, 0.28F, e.beamLength(), 0.95F, 0.99F, 1.0F, 1.0F);
        } else if (e.visualType() == 2) {
            // Charging effect: fast, intersecting energy rings instead of a floating orb.
            float charge = e.charge();
            float pulse = 1.0F + Mth.sin((e.tickCount + partial) * 0.38F) * 0.06F;
            float radius = (0.34F + charge * 0.28F) * pulse;
            float spin = (e.tickCount + partial) * 0.12F;
            stack.mulPose(Axis.YP.rotation(spin));
            pose = stack.last();
            drawRing(out, pose, radius, 0.16F, 0.25F, 0.65F, 1.0F, 0.72F);
            stack.mulPose(Axis.XP.rotation(1.05F + spin * 0.7F));
            pose = stack.last();
            drawRing(out, pose, radius * 0.78F, 0.12F, 0.55F, 0.84F, 1.0F, 0.88F);
            stack.mulPose(Axis.ZP.rotation(-spin * 1.4F));
            pose = stack.last();
            drawRing(out, pose, radius * 0.52F, 0.085F, 0.88F, 0.97F, 1.0F, 1.0F);
        } else {
            // Ki Blast projectile: a compact streak of energy, not a sphere.
            net.minecraft.world.phys.Vec3 d = e.getDeltaMovement().normalize();
            stack.mulPose(Axis.YP.rotation((float)Math.atan2(-d.x, d.z)));
            stack.mulPose(Axis.XP.rotation((float)Math.atan2(-d.y, Math.sqrt(d.x*d.x + d.z*d.z))));
            pose = stack.last();
            float pulse = 1.0F + Mth.sin((e.tickCount + partial) * 0.45F) * 0.08F;
            drawTube(out, pose, 0.28F * pulse, 1.35F, 0.20F, 0.60F, 1.0F, 0.78F);
            drawTube(out, pose, 0.14F * pulse, 1.45F, 0.82F, 0.94F, 1.0F, 1.0F);
        }
        stack.popPose();
        super.render(e, yaw, partial, stack, buffers, light);
    }

    private static void drawRing(VertexConsumer out, PoseStack.Pose p, float radius, float tubeRadius, float cr, float cg, float cb, float alpha) {
        int segments = 36;
        for (int i = 0; i < segments; i++) {
            double a0 = 2.0 * Math.PI * i / segments;
            double a1 = 2.0 * Math.PI * (i + 1) / segments;
            for (int side = 0; side < 4; side++) {
                double s0 = side * Math.PI / 2.0;
                double s1 = (side + 1) * Math.PI / 2.0;
                float x00 = (float)((radius + tubeRadius * Math.cos(s0)) * Math.cos(a0));
                float y00 = (float)((radius + tubeRadius * Math.cos(s0)) * Math.sin(a0));
                float z00 = (float)(tubeRadius * Math.sin(s0));
                float x01 = (float)((radius + tubeRadius * Math.cos(s1)) * Math.cos(a0));
                float y01 = (float)((radius + tubeRadius * Math.cos(s1)) * Math.sin(a0));
                float z01 = (float)(tubeRadius * Math.sin(s1));
                float x11 = (float)((radius + tubeRadius * Math.cos(s1)) * Math.cos(a1));
                float y11 = (float)((radius + tubeRadius * Math.cos(s1)) * Math.sin(a1));
                float z11 = (float)(tubeRadius * Math.sin(s1));
                float x10 = (float)((radius + tubeRadius * Math.cos(s0)) * Math.cos(a1));
                float y10 = (float)((radius + tubeRadius * Math.cos(s0)) * Math.sin(a1));
                float z10 = (float)(tubeRadius * Math.sin(s0));
                ringVertex(out, p, x00, y00, z00, cr, cg, cb, alpha);
                ringVertex(out, p, x01, y01, z01, cr, cg, cb, alpha);
                ringVertex(out, p, x11, y11, z11, cr, cg, cb, alpha);
                ringVertex(out, p, x10, y10, z10, cr, cg, cb, alpha);
            }
        }
    }
    private static void ringVertex(VertexConsumer out, PoseStack.Pose p, float x, float y, float z, float cr, float cg, float cb, float alpha) {
        out.vertex(p.pose(), x, y, z).color(cr, cg, cb, alpha).uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
            .normal(p.normal(), x, y, z).endVertex();
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
