package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.PropertyContainer;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.api.StateUpdateableChunkManager;
import virtuoel.towelette.api.UpdateableFluid;
import virtuoel.towelette.mixin.layer.ModifiableWorldMixin;

@Mixin(World.class)
public abstract class WorldMixin<O, S extends PropertyContainer<S>> implements ModifiableWorldMixin<O, S>
{
	@Shadow abstract FluidState getFluidState(BlockPos pos);
	
	@Inject(method = "updateNeighbor", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private void onUpdateNeighborAfterNeighborUpdate(BlockPos pos, Block block, BlockPos otherPos, CallbackInfo info)
	{
		final FluidState state = getFluidState(pos); // TODO FIXME layers
		final UpdateableFluid f = (UpdateableFluid) state.getFluid();
		f.neighborUpdate(state, (World) (Object) this, pos, otherPos);
	}
	
	@Inject(method = "doesAreaContainFireSource", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private void doesAreaContainFireSourceGetFluidState(Box box, CallbackInfoReturnable<Boolean> info, int noop1, int noop2, int noop3, int noop4, int noop5, int noop6, BlockPos.PooledMutable pos, int noop7, int noop8, int noop9)
	{
		if(getFluidState(pos).matches(FluidTags.LAVA))
		{
			info.setReturnValue(true);
		}
	}
	
	@Shadow abstract boolean setBlockState(BlockPos pos, BlockState state, int flags);
	
	@Inject(at = @At(value = "HEAD"), method = "clearBlockState", cancellable = true)
	private void onClearBlockState(BlockPos pos, boolean flag, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(setBlockState(pos, Blocks.AIR.getDefaultState(), 3 | (flag ? 64 : 0)));
	}
	
	@ModifyArg(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private BlockState breakBlockSetBlockState(BlockState blockState)
	{
		return Blocks.AIR.getDefaultState();
	}
	
	@Override
	public S getState(Identifier layer, BlockPos pos)
	{
		final World self = World.class.cast(this);
		if(World.isHeightInvalid(pos))
		{
			final S state = PaletteRegistrar.<O, S>getPaletteData(layer).getInvalidPositionState();
			return state;
		}
		else
		{
			@SuppressWarnings("unchecked")
			final ChunkStateLayer<O, S> chunk = ((ChunkStateLayer<O, S>) self.getWorldChunk(pos));
			return chunk.getState(layer, pos);
		}
	}
	
	@Override
	public boolean setState(Identifier layer, BlockPos pos, S state, int flags)
	{
		final World self = World.class.cast(this);
		if(World.isHeightInvalid(pos))
		{
			return false;
		}
		else if(!self.isClient && self.getLevelProperties().getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES)
		{
			return false;
		}
		else
		{
			final WorldChunk chunk = self.getWorldChunk(pos);
			@SuppressWarnings("unchecked")
			final ChunkStateLayer<O, S> c = ((ChunkStateLayer<O, S>) chunk);
			final S oldState = c.setState(layer, pos, state, (flags & 64) != 0);
			if(oldState == null)
			{
				return false;
			}
			else
			{
				final S newState = c.getState(layer, pos);
				
				if(PaletteRegistrar.<O, S>getPaletteData(layer).shouldEnqueueLightUpdate(self, pos, newState, oldState))
				{
					self.getChunkManager().getLightingProvider().enqueueLightUpdate(pos);
				}
				
				if(newState == state)
				{
					if(oldState != newState)
					{
						self.scheduleBlockRender(pos, Blocks.AIR.getDefaultState(), Blocks.VOID_AIR.getDefaultState());
					}
					
					if ((flags & 2) != 0 && (!self.isClient || (flags & 4) == 0) && (self.isClient || chunk.getLevelType() != null && chunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING)))
					{
						if(self instanceof ServerWorld)
						{
							((StateUpdateableChunkManager) ((ServerWorld) self).method_14178()).onStateUpdate(layer, pos);
						}
						else
						{
							self.updateListeners(pos, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), flags);
						}
					}
					
					if (!self.isClient && (flags & 1) != 0)
					{
						// TODO
						updateNeighbors(layer, self, pos);
					}
				}
				
				return true;
			}
		}
	}
	
	@Unique
	private static void updateNeighbors(Identifier layer, World world, BlockPos pos)
	{
		if (world.getLevelProperties().getGeneratorType() != LevelGeneratorType.DEBUG_ALL_BLOCK_STATES)
		{
			updateNeighborsAlways(layer, world, pos);
		}
		
	}
	
	@Unique
	private static void updateNeighborsAlways(Identifier layer, World world, BlockPos pos)
	{
		updateNeighbor(layer, world, pos.west(), pos);
		updateNeighbor(layer, world, pos.east(), pos);
		updateNeighbor(layer, world, pos.down(), pos);
		updateNeighbor(layer, world, pos.up(), pos);
		updateNeighbor(layer, world, pos.north(), pos);
		updateNeighbor(layer, world, pos.south(), pos);
	}
	
	@Unique
	private static void updateNeighborsExcept(Identifier layer, World world, BlockPos pos, Direction direction)
	{
		if (direction != Direction.WEST)
		{
			updateNeighbor(layer, world, pos.west(), pos);
		}
		
		if (direction != Direction.EAST)
		{
			updateNeighbor(layer, world, pos.east(), pos);
		}
		
		if (direction != Direction.DOWN)
		{
			updateNeighbor(layer, world, pos.down(), pos);
		}
		
		if (direction != Direction.UP)
		{
			updateNeighbor(layer, world, pos.up(), pos);
		}
		
		if (direction != Direction.NORTH)
		{
			updateNeighbor(layer, world, pos.north(), pos);
		}
		
		if (direction != Direction.SOUTH)
		{
			updateNeighbor(layer, world, pos.south(), pos);
		}
		
	}
	
	@Unique
	private static <O, S extends PropertyContainer<S>> void updateNeighbor(Identifier layer, World world, BlockPos pos, BlockPos otherPos)
	{
		if (!world.isClient())
		{
			final PaletteData<O, S> data = PaletteRegistrar.getPaletteData(layer);
			@SuppressWarnings("unchecked")
			final BlockViewStateLayer<S> w = ((BlockViewStateLayer<S>) world);
			data.onNeighborUpdate(w.getState(layer, pos), world, pos, data.getEntry(w.getState(layer, otherPos)), otherPos, false);
		}
	}
}
