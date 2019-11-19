package virtuoel.towelette.mixin.layer.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.ChunkOcclusionGraphBuilder;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRenderTask;
import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.util.client.RenderUtils;

@Mixin(ChunkRenderer.class)
public abstract class ChunkRendererMixin
{
	@Shadow @Final Set<BlockEntity> blockEntities;
	@Shadow @Final WorldRenderer renderer;
	@Shadow abstract void beginBufferBuilding(BufferBuilder builder, BlockPos pos);
	@Shadow abstract void endBufferBuilding(BlockRenderLayer renderLayer, float x, float y, float z, BufferBuilder builder, ChunkRenderData data);
	
	@Inject(at = @At("HEAD"), method = "rebuildChunk(FFFLnet/minecraft/client/render/chunk/ChunkRenderTask;)V", cancellable = true)
	public <O, S extends PropertyContainer<S>> void onRebuildChunk(float x, float y, float z, ChunkRenderTask task, CallbackInfo info)
	{
		final ChunkRenderer self = (ChunkRenderer) (Object) this;
		final ChunkRenderData renderData = new ChunkRenderData();
		final BlockPos origin = self.getOrigin().toImmutable();
		final BlockPos corner = origin.add(15, 15, 15);
		if (self.getWorld() != null)
		{
			task.getLock().lock();
			
			try
			{
				if (task.getStage() != ChunkRenderTask.Stage.COMPILING)
				{
					return;
				}
				
				task.setRenderData(renderData);
			}
			finally
			{
				task.getLock().unlock();
			}
			
			final ChunkOcclusionGraphBuilder builder = new ChunkOcclusionGraphBuilder();
			final HashSet<BlockEntity> renderedBlockEntities = Sets.newHashSet();
			final ChunkRendererRegion region = task.takeRegion();
			if (region != null)
			{
				ChunkRenderer.chunkUpdateCount++;
				final boolean[] layerFlags = new boolean[BlockRenderLayer.values().length];
				BlockModelRenderer.enableBrightnessCache();
				final Random random = new Random();
				final BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
				final Iterator<BlockPos> iter = BlockPos.iterate(origin, corner).iterator();
				
				while (iter.hasNext())
				{
					final BlockPos pos = iter.next();
					
					{ // TODO Remove
						final BlockState blockState = region.getBlockState(pos);
						if (blockState.isFullOpaque(region, pos))
						{
							builder.markClosed(pos);
						}
						
						if (blockState.getBlock().hasBlockEntity())
						{
							final BlockEntity blockEntity = region.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
							if (blockEntity != null)
							{
								BlockEntityRenderer<BlockEntity> renderer = BlockEntityRenderDispatcher.INSTANCE.get(blockEntity);
								if (renderer != null)
								{
									renderData.addBlockEntity(blockEntity);
									if (renderer.method_3563(blockEntity))
									{
										renderedBlockEntities.add(blockEntity);
									}
								}
							}
						}
					}
					
					for (final Identifier id : PaletteRegistrar.PALETTES.getIds())
					{
						final PaletteData<O, S> layer = PaletteRegistrar.<O, S>getPaletteData(id);
						
						BlockViewStateLayer world = ((BlockViewStateLayer) region);
						final S state = world.getState(layer, pos);
						
					//	data.handleOcclusionGraph(builder, state, region, pos);
						
						// TODO occlusion and BE render callbacks
						
						if (layer.shouldRender(state))
						{
							final BlockRenderLayer blockRenderLayer = layer.getRenderLayer(state);
							final int layerOrdinal = blockRenderLayer.ordinal();
							final BufferBuilder bufferBuilder = task.getBufferBuilders().get(layerOrdinal);
							if (!renderData.isBufferInitialized(blockRenderLayer))
							{
								renderData.markBufferInitialized(blockRenderLayer);
								beginBufferBuilding(bufferBuilder, origin);
							}
							
							layerFlags[layerOrdinal] |= layer.tesselate(blockRenderManager, state, pos, region, bufferBuilder, random);
						}
					}
				}
				
				for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values())
				{
					if (layerFlags[blockRenderLayer.ordinal()])
					{
						RenderUtils.setNonEmpty(renderData, blockRenderLayer);
					}
					
					if (renderData.isBufferInitialized(blockRenderLayer))
					{
						endBufferBuilding(blockRenderLayer, x, y, z, task.getBufferBuilders().get(blockRenderLayer), renderData);
					}
				}
				
				BlockModelRenderer.disableBrightnessCache();
			}
			
			renderData.setOcclusionGraph(builder.build());
			self.getLock().lock();
			
			try
			{
				final Set<BlockEntity> add = Sets.newHashSet(renderedBlockEntities);
				final Set<BlockEntity> remove = Sets.newHashSet(this.blockEntities);
				add.removeAll(this.blockEntities);
				remove.removeAll(renderedBlockEntities);
				this.blockEntities.clear();
				this.blockEntities.addAll(renderedBlockEntities);
				renderer.updateBlockEntities(remove, add);
			}
			finally
			{
				self.getLock().unlock();
			}
		}
		
		info.cancel();
	}
}
