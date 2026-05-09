package fr.tchkll.skygrad.renderer;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public class WitherCrystalRenderer extends EndCrystalRenderer {

    private static final ResourceLocation WITHER_CRYSTAL_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("skygrad", "textures/entity/wither_crystal/wither_crystal.png");

    private static final RenderType WITHER_RENDER_TYPE =
        RenderType.entityCutoutNoCull(WITHER_CRYSTAL_TEXTURE);
    private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public WitherCrystalRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        ModelPart modelpart = ctx.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = modelpart.getChild("glass");
        this.cube = modelpart.getChild("cube");
        this.base = modelpart.getChild("base");
    }

    @Override
    public void render(EndCrystal entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float f = EndCrystalRenderer.getY(entity, partialTicks);
        float f1 = ((float)entity.time + partialTicks) * 3.0F;
        VertexConsumer vertexconsumer = buffer.getBuffer(WITHER_RENDER_TYPE); // ← ta texture
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        int i = OverlayTexture.NO_OVERLAY;
        int fullBright = LightTexture.FULL_BRIGHT; // ← ajoute ça
        int lowLight = LightTexture.FULL_BLOCK;
        if (entity.showsBottom()) {
            this.base.render(poseStack, vertexconsumer, fullBright, i);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        poseStack.translate(0.0F, 1.5F + f / 2.0F, 0.0F);
        poseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        this.glass.render(poseStack, vertexconsumer, lowLight, i);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.glass.render(poseStack, vertexconsumer, fullBright, i);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.cube.render(poseStack, vertexconsumer, fullBright, i);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EndCrystal entity) {
        return WITHER_CRYSTAL_TEXTURE;
    }
}