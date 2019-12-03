package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;
import net.minecraft.world.World;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.util.FluidUtils;
import virtuoel.towelette.util.StateNeighborGroup;

@Mixin(BaseFluid.class)
public abstract class BaseFluidMixin
{
	@Redirect(method = "receivesFlow", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShapes;adjacentSidesCoverSquare(Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/math/Direction;)Z"))
	private boolean receivesFlowAdjacentSidesCoverSquareProxy(VoxelShape shape, VoxelShape otherShape, Direction direction, Direction noop, BlockView world, BlockPos blockPos, BlockState blockState, BlockPos otherPos, BlockState otherState)
	{
		return FluidUtils.isFluidFlowBlocked(direction, world, shape, blockState, blockPos, otherShape, otherState, otherPos);
	}
	
	@Redirect(method = "flow", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
	private Block onFlowGetBlockProxy(BlockState obj, IWorld world, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
	{
		final Block block = obj.getBlock();
		final boolean fillable = block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(world, pos, blockState, fluidState.getFluid());
		return fillable ? block : null;
	}
	
	@Unique private final ThreadLocal<Block> towelette$cachedBlock = ThreadLocal.withInitial(() -> Blocks.AIR);
	
	@ModifyVariable(method = "method_15754", at = @At(value = "LOAD", ordinal = 0), allow = 1, require = 1)
	private Block hookMethod_15754NotFillable(Block block)
	{
		towelette$cachedBlock.set(block);
		return null;
	}
	
	@ModifyVariable(method = "method_15754", at = @At(value = "LOAD", ordinal = 2), allow = 1, require = 1)
	private Block hookMethod_15754RevertBlock(Block block)
	{
		final Block cache = towelette$cachedBlock.get();
		towelette$cachedBlock.remove();
		return cache;
	}
	
	@Inject(at = @At("RETURN"), method = "method_15754", cancellable = true)
	private void onMethod_15754(BlockView blockView, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> info)
	{
		final Block block = state.getBlock();
		final boolean displaceable = info.getReturnValueZ();
		final boolean fillable = block instanceof FluidFillable;
		final boolean canFill = fillable && ((FluidFillable) block).canFillWithFluid(blockView, pos, state, fluid);
		if (!displaceable)
		{
			if (canFill)
			{
				info.setReturnValue(true);
				return;
			}
			
			final boolean empty = blockView.getFluidState(pos).isEmpty();
			if (empty && block.matches(Towelette.DISPLACEABLE) && !block.matches(Towelette.UNDISPLACEABLE))
			{
				info.setReturnValue(true);
				return;
			}
		}
		else if (fillable && !canFill)
		{
			final boolean empty = blockView.getFluidState(pos).isEmpty();
			if (!empty || block.matches(Towelette.UNDISPLACEABLE))
			{
				info.setReturnValue(false);
				return;
			}
		}
	}
	
	@Shadow abstract boolean isInfinite();
	@Shadow abstract boolean method_15752(FluidState state);
	@Shadow abstract int getLevelDecreasePerBlock(WorldView world);
	@Shadow abstract boolean receivesFlow(Direction direction, BlockView world, BlockPos blockPos, BlockState blockState, BlockPos otherPos, BlockState otherState);
	
	@Inject(method = "getUpdatedState", at = @At(value = "HEAD"), cancellable = true)
	private void onGetUpdatedState(WorldView world, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> info)
	{
		final BaseFluid self = (BaseFluid) (Object) this;
		int maxLevel = 0;
		int sources = 0;
		
		for (final Direction dir : Direction.Type.HORIZONTAL)
		{
			final BlockPos blockPos = pos.offset(dir);
			final BlockState blockState = world.getBlockState(blockPos);
			final FluidState fluidState = world.getFluidState(blockPos);
			if (fluidState.getFluid().matchesType(self) && this.receivesFlow(dir, world, pos, state, blockPos, blockState))
			{
				if (fluidState.isStill())
				{
					sources++;
				}
				
				maxLevel = Math.max(maxLevel, fluidState.getLevel());
			}
		}
		
		if (isInfinite() && sources >= 2)
		{
			final BlockPos blockPos = pos.down();
			final BlockState blockState = world.getBlockState(blockPos);
			final FluidState fluidState = world.getFluidState(blockPos);
			if (blockState.getMaterial().isSolid() || method_15752(fluidState))
			{
				info.setReturnValue(self.getStill(false));
			}
		}
		
		final BlockPos blockPos = pos.up();
		final BlockState blockState = world.getBlockState(blockPos);
		final FluidState fluidState = world.getFluidState(blockPos);
		if (!fluidState.isEmpty() && fluidState.getFluid().matchesType(self) && this.receivesFlow(Direction.UP, world, pos, state, blockPos, blockState))
		{
			info.setReturnValue(self.getFlowing(8, true));
		}
		else
		{
			final int level = maxLevel - getLevelDecreasePerBlock(world);
			info.setReturnValue(level <= 0 ? Fluids.EMPTY.getDefaultState() : self.getFlowing(level, false));
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "receivesFlow", cancellable = true)
	private void onReceivesFlow(Direction direction, BlockView world, BlockPos blockPos, BlockState blockState, BlockPos otherPos, BlockState otherState, CallbackInfoReturnable<Boolean> info)
	{
		final Object2ByteLinkedOpenHashMap<StateNeighborGroup> map;
		if (!blockState.getBlock().hasDynamicBounds() && !otherState.getBlock().hasDynamicBounds())
		{
			map = FluidUtils.FLUID_FLOW_MAP.get();
		}
		else
		{
			map = null;
		}
		
		final StateNeighborGroup group;
		if (map != null)
		{
			group = new StateNeighborGroup(direction, blockState, otherState, world.getFluidState(blockPos), world.getFluidState(otherPos));
			byte value = map.getAndMoveToFirst(group);
			if (value != 127)
			{
				info.setReturnValue(value != 0);
			}
		}
		else
		{
			group = null;
		}
		
		final VoxelShape shape = blockState.getCollisionShape(world, blockPos);
		final VoxelShape otherShape = otherState.getCollisionShape(world, otherPos);
		final boolean canFlow = !FluidUtils.isFluidFlowBlocked(direction, world, shape, blockState, blockPos, otherShape, otherState, otherPos);
		if (map != null)
		{
			if (map.size() == 200)
			{
				map.removeLastByte();
			}
			
			map.putAndMoveToFirst(group, (byte) (canFlow ? 1 : 0));
		}
		
		info.setReturnValue(canFlow);
	}
	
	@Shadow abstract void beforeBreakingBlock(IWorld world, BlockPos pos, BlockState state);
	
	@Inject(at = @At(value = "HEAD"), method = "flow", cancellable = true)
	private void onFlowPre(IWorld world, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState, CallbackInfo info)
	{
		final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
		w.setState(LayerRegistrar.FLUID, pos, fluidState, fluidState.isEmpty() ? 3 : 2);
		
		if (blockState.matches(Towelette.DISPLACEABLE) && !blockState.matches(Towelette.UNDISPLACEABLE))
		{
			if (!blockState.isAir())
			{
				beforeBreakingBlock(world, pos, blockState);
			}
			
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		}
		else
		{
			info.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "method_15754", cancellable = true)
	private void onMethod_15754Pre(BlockView blockView, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(state.getCollisionShape(blockView, pos) != VoxelShapes.fullCube());
	}
	
	@Inject(method = "method_15734(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;I)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "RETURN"), cancellable = true)
	private static void onCompute1(WorldView world, BlockPos pos, int key, CallbackInfoReturnable<Pair<BlockState, FluidState>> info)
	{
		info.setReturnValue(Pair.of(info.getReturnValue().getFirst(), world.getFluidState(pos)));
	}
	
	@Inject(method = "method_15755(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;I)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "RETURN"), cancellable = true)
	private static void onCompute2(WorldView world, BlockPos pos, int key, CallbackInfoReturnable<Pair<BlockState, FluidState>> info)
	{
		info.setReturnValue(Pair.of(info.getReturnValue().getFirst(), world.getFluidState(pos)));
	}
	
	@Inject(method = "method_15736", at = @At(value = "RETURN"), cancellable = true)
	private void onMethod_15736(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos otherPos, BlockState otherState, CallbackInfoReturnable<Boolean> info)
	{
		final BaseFluid self = (BaseFluid) (Object) this;
		if (info.getReturnValueZ() && otherState.getFluidState().getFluid().matchesType(self))
		{
			info.setReturnValue(world.getFluidState(otherPos).getFluid().matchesType(self));
		}
	}
	
	@Inject(method = "flow", at = @At(value = "RETURN"))
	private void onFlow(IWorld world, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState, CallbackInfo info)
	{
		if (!(blockState.getBlock() instanceof FluidFillable))
		{
			final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
			w.setState(LayerRegistrar.FLUID, pos, fluidState, fluidState.isEmpty() ? 3 : 2);
		}
	}
	
	@Redirect(method = "onScheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean onOnScheduledTickSetBlockStateProxy(World obj, BlockPos pos, BlockState state, int flags)
	{
		return false;
	}
	
	@Inject(method = "onScheduledTick", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private void onOnScheduledTick(World world, BlockPos pos, FluidState fluidState, CallbackInfo info)
	{
		final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
		w.setState(LayerRegistrar.FLUID, pos, fluidState, fluidState.isEmpty() ? 3 : 2);
	}
}
