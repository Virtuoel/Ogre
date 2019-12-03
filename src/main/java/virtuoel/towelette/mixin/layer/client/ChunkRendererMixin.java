package virtuoel.towelette.mixin.layer.client;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public abstract class ChunkRendererMixin
{
	@Shadow abstract <E extends BlockEntity> void addBlockEntity(ChunkBuilder.ChunkData chunkData, Set<BlockEntity> set, E blockEntity);
	
	@Redirect(
		method = "render(FFFLnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;Lnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Ljava/util/Set;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/BlockPos;iterate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"
		)
	)
	private Iterable<BlockPos> iterateProxy(BlockPos blockPos, BlockPos blockPos2)
	{
		return Collections::emptyIterator;
	}
	
	@Inject(locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Ljava/util/Iterator;hasNext()Z"), method = "render(FFFLnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;Lnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Ljava/util/Set;")
	private <O, S extends State<S>> void render(float f, float g, float h, ChunkBuilder.ChunkData chunkData, BlockBufferBuilderStorage blockBufferBuilderStorage, CallbackInfoReturnable<Set<BlockEntity>> info, int i, BlockPos origin, BlockPos corner, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set<BlockEntity> blockEntities, ChunkRendererRegion world, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager)
	{
		final ChunkDataAccessor accessibleChunkData = (ChunkDataAccessor) chunkData;
		
		for (final BlockPos pos : BlockPos.iterate(origin, corner))
		{
			// TODO Remove
			if (world.getBlockState(pos).getBlock().hasBlockEntity())
			{
				final BlockEntity blockEntity = world.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
				if (blockEntity != null)
				{
					this.addBlockEntity(chunkData, blockEntities, blockEntity);
				}
			}
			
			for (final Identifier id : LayerRegistrar.LAYERS.getIds())
			{
				final LayerData<O, S> layer = LayerRegistrar.<O, S>getLayerData(id);
				
				final S state = ((BlockViewStateLayer) world).getState(layer, pos);
				
				layer.handleOcclusionGraph(chunkOcclusionDataBuilder, state, world, pos);
				
				if (layer.shouldRender(state))
				{
					final RenderLayer renderLayer = layer.getRenderLayer(state);
					final BufferBuilder bufferBuilder = blockBufferBuilderStorage.get(renderLayer);
					if (accessibleChunkData.getInitializedLayers().add(renderLayer))
					{
						bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
					}
					
					if (layer.tesselate(blockRenderManager, state, pos, world, matrixStack, bufferBuilder, true, random))
					{
						accessibleChunkData.setEmpty(false);
						accessibleChunkData.getNonEmptyLayers().add(renderLayer);
					}
				}
			}
		}
	}
}
