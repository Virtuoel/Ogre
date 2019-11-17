package virtuoel.towelette.util;

import java.util.Random;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkOcclusionGraphBuilder;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;

public class PaletteUtils
{
	public static void handleBlockStateOcclusionGraph(Object chunkOcclusionGraphBuilder, BlockState state, BlockView world, BlockPos pos)
	{
		if (state.isFullOpaque(world, pos))
		{
			((ChunkOcclusionGraphBuilder) chunkOcclusionGraphBuilder).markClosed(pos);
		}
	}
	
	public static BlockRenderLayer getBlockStateRenderLayer(BlockState state)
	{
		return state.getBlock().getRenderLayer();
	}
	
	public static boolean shouldRenderBlockState(BlockState state)
	{
		return state.getRenderType() != BlockRenderType.INVISIBLE;
	}
	
	public static boolean shouldRenderFluidState(FluidState state)
	{
		return !state.isEmpty();
	}
	
	public static boolean tesselateBlockState(Object blockRenderManager, BlockState state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random)
	{
		final BlockRenderManager manager = (BlockRenderManager) blockRenderManager;
		final BufferBuilder builder = (BufferBuilder) bufferBuilder;
		
		return manager.tesselateBlock(state, pos, world, builder, random);
	}
	
	public static boolean tesselateFluidState(Object blockRenderManager, FluidState state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random)
	{
		final BlockRenderManager manager = (BlockRenderManager) blockRenderManager;
		final BufferBuilder builder = (BufferBuilder) bufferBuilder;
		
		return manager.tesselateFluid(pos, world, builder, state);
	}
}
