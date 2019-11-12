package virtuoel.towelette.command.arguments;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.CachedFluidPosition;
import virtuoel.towelette.api.ModifiableWorldFluidLayer;

public class FluidArgument implements Predicate<CachedBlockPosition>
{
	private final FluidState state;
	private final Set<Property<?>> properties;
	
	public FluidArgument(FluidState state, Set<Property<?>> properties)
	{
		this.state = state;
		this.properties = properties;
	}
	
	public FluidState getFluidState()
	{
		return this.state;
	}
	
	@Override
	public boolean test(CachedBlockPosition pos)
	{
		FluidState fluidState = ((CachedFluidPosition) pos).getFluidState();
		if(fluidState.getFluid() != this.state.getFluid())
		{
			return false;
		}
		else
		{
			for(Property<?> property : this.properties)
			{
				if(fluidState.get(property) != this.state.get(property))
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	public boolean setFluidState(ServerWorld world, BlockPos pos, int flags)
	{
		return ((ModifiableWorldFluidLayer) world).setFluidState(pos, this.state, flags);
	}
}
