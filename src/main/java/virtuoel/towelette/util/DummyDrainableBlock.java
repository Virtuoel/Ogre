package virtuoel.towelette.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;

public class DummyDrainableBlock extends Block implements FluidDrainable
{
	public static final DummyDrainableBlock INSTANCE = new DummyDrainableBlock(Block.Settings.of(Material.WATER));
	
	private DummyDrainableBlock(Block.Settings settings)
	{
		super(settings);
	}
	
	@Override
	public Fluid tryDrainFluid(IWorld world, BlockPos pos, BlockState state)
	{
		final Fluid fluid;
		final FluidState fluidState = world.getFluidState(pos);
		if(fluidState.isStill())
		{
			fluid = fluidState.getFluid();
		}
		else
		{
			fluid = Fluids.EMPTY;
		}
		
		if(fluid != Fluids.EMPTY)
		{
			final BlockState blockState = world.getBlockState(pos);
			final Block block = blockState.getBlock();
			if (block instanceof FluidDrainable)
			{
				((FluidDrainable) block).tryDrainFluid(world, pos, blockState);
			}
			
			final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
			w.setState(LayerRegistrar.FLUID, pos, Fluids.EMPTY.getDefaultState(), 11);
		}
		
		return fluid;
	}
}
