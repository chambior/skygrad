package fr.tchkll.skygrad.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.tchkll.skygrad.blockentity.TowerSentinelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public class TowerSentinelRenderer implements BlockEntityRenderer<TowerSentinelBlockEntity> {

    private final WitherCrystalRenderer crystalRenderer;
    private EndCrystal crystalEntity;
    public TowerSentinelRenderer(BlockEntityRendererProvider.Context ctx) {
        EntityRendererProvider.Context ectx = new EntityRendererProvider.Context(
            Minecraft.getInstance().getEntityRenderDispatcher(),
            Minecraft.getInstance().getItemRenderer(),
            Minecraft.getInstance().getBlockRenderer(),
            Minecraft.getInstance().gameRenderer.itemInHandRenderer,
            Minecraft.getInstance().getResourceManager(),
            Minecraft.getInstance().getEntityModels(),
            Minecraft.getInstance().font
        );
        this.crystalRenderer = new WitherCrystalRenderer(ectx);
    }

     @Override
    public void render(TowerSentinelBlockEntity be, float partialTick, PoseStack poseStack,
                    MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // ── Base statique ─────────────────────────────────────────────
        poseStack.pushPose();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.getModelRenderer().renderModel(
            poseStack.last(),
            bufferSource.getBuffer(RenderType.solid()),
            be.getBlockState(),
            dispatcher.getBlockModel(be.getBlockState()),
            1f, 1f, 1f,
            packedLight, packedOverlay
        );
        poseStack.popPose();

        // ── Ender Crystal ─────────────────────────────────────────────
        if (crystalEntity == null) {
            crystalEntity = new EndCrystal(EntityType.END_CRYSTAL, be.getLevel());
            crystalEntity.setShowBottom(false);
        }

        crystalEntity.tickCount = (int)(be.getLevel().getGameTime() % Integer.MAX_VALUE);
        crystalEntity.time = (int)(be.getLevel().getGameTime() % Integer.MAX_VALUE);
        crystalEntity.setPos(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ());

        poseStack.pushPose();
        poseStack.translate(0.5, 1.3, 0.5);
        poseStack.scale(0.8f, 0.8f, 0.8f);

        crystalRenderer.render(crystalEntity, 0f, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() { return 128; }
}