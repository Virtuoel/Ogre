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
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.LayerRegistrar;

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
		final FluidState fluidState = world.getFluidState(pos);
		if(!fluidState.isEmpty())
		{
			final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
			w.setState(LayerRegistrar.FLUID, pos, Fluids.EMPTY.getDefaultState(), 11);
		}
		
		return fluidState.getFluid();
	}
}
