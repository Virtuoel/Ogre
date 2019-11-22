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
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import virtuoel.towelette.api.UpdateableFluid;

public class LayerUtils
{
	public static void blockStateHeightmapUpdate(Chunk chunk, int x, int y, int z, BlockState state)
	{
		chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, state);
		chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, state);
		chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, state);
		chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, state);
	}
	
	public static void onBlockStateAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean pushed)
	{
		state.onBlockAdded(world, pos, oldState, pushed);
	}
	
	public static void onBlockStateNeighborUpdate(BlockState state, World world, BlockPos pos, BlockState otherState, BlockPos otherPos, boolean pushed)
	{
		state.neighborUpdate(world, pos, otherState.getBlock(), otherPos, pushed);
	}
	
	public static void updateNeighborBlockStates(World world, BlockPos pos, BlockState state, BlockState oldState, int flags)
	{
		oldState.method_11637(world, pos, flags);
		state.updateNeighborStates(world, pos, flags);
		state.method_11637(world, pos, flags);
	}
	
	public static void onFluidStateAdded(FluidState state, World world, BlockPos pos, FluidState oldState, boolean pushed)
	{
		((UpdateableFluid) state.getFluid()).onFluidAdded(state, world, pos, oldState);
	}
	
	public static void onFluidStateNeighborUpdate(FluidState state, World world, BlockPos pos, FluidState otherState, BlockPos otherPos, boolean pushed)
	{
		((UpdateableFluid) state.getFluid()).neighborUpdate(state, world, pos, otherPos);
	}
	
	public static void updateNeighborFluidStates(World world, BlockPos pos, FluidState state, FluidState oldState, int flags)
	{
		((UpdateableFluid) state.getFluid()).updateNeighborStates(world, pos, state, flags);
	}
	
	public static boolean shouldUpdateBlockStateLight(BlockView world, BlockPos pos, BlockState newState, BlockState oldState)
	{
		return newState != oldState && (newState.getLightSubtracted(world, pos) != oldState.getLightSubtracted(world, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency());
	}
	
	public static boolean shouldUpdateFluidStateLight(BlockView world, BlockPos pos, FluidState newState, FluidState oldState)
	{
		return newState != oldState && (newState.getBlockState().getLightSubtracted(world, pos) != oldState.getBlockState().getLightSubtracted(world, pos) || newState.getBlockState().getLuminance() != oldState.getBlockState().getLuminance() || newState.getBlockState().hasSidedTransparency() || oldState.getBlockState().hasSidedTransparency());
	}
	
	public static class Client
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
		
		public static boolean tesselateBlockState(Object blockRenderManager, BlockState state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random)
		{
			final BlockRenderManager manager = (BlockRenderManager) blockRenderManager;
			final BufferBuilder builder = (BufferBuilder) bufferBuilder;
			
			return manager.tesselateBlock(state, pos, world, builder, random);
		}
		
		public static BlockRenderLayer getFluidStateRenderLayer(FluidState state)
		{
			return state.getRenderLayer();
		}
		
		public static boolean shouldRenderFluidState(FluidState state)
		{
			return !state.isEmpty();
		}
		
		public static boolean tesselateFluidState(Object blockRenderManager, FluidState state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random)
		{
			final BlockRenderManager manager = (BlockRenderManager) blockRenderManager;
			final BufferBuilder builder = (BufferBuilder) bufferBuilder;
			
			return manager.tesselateFluid(pos, world, builder, state);
		}
	}
}
