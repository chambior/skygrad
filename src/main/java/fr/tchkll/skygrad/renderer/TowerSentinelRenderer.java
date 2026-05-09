// package fr.tchkll.skygrad.renderer;

// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.math.Axis;

// import fr.tchkll.skygrad.blockentity.TowerSentinelBlockEntity;
// import net.minecraft.client.renderer.MultiBufferSource;
// import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
// import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

// public class TowerSentinelRenderer implements BlockEntityRenderer<TowerSentinelBlockEntity> {

//     public TowerSentinelRenderer(BlockEntityRendererProvider.Context ctx) {}

//     @Override
//     public void render(TowerSentinelBlockEntity be, float partialTick, PoseStack poseStack,
//                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

//         float time = (be.getLevel().getGameTime() + partialTick) / 20f;

//         // ── Rendu de la partie haute (Material.001) — avec rotation ─
//         poseStack.pushPose();
//         poseStack.translate(0.5, 1.0, 0.5); // centre de rotation
//         float angle = (time * 360f / 4f) % 360f; // 1 tour / 4 secondes
//         poseStack.mulPose(Axis.YP.rotationDegrees(angle));
//         poseStack.translate(-0.5, -1.0, -0.5);
//         // rendu ici
//         poseStack.popPose();
//     }

//     @Override
//     public int getViewDistance() { return 128; }
// }