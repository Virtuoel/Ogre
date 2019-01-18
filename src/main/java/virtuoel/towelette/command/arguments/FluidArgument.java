package virtuoel.towelette.command.arguments;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.CachedFluidPosition;
import virtuoel.towelette.util.StateUtils;

public class FluidArgument implements Predicate<CachedBlockPosition>
{
	private final FluidState state;
	private final Set<Property<?>> properties;
	
	public FluidArgument(FluidState blockState_1, Set<Property<?>> set_1)
	{
		this.state = blockState_1;
		this.properties = set_1;
	}
	
	public FluidState getFluidState()
	{
		return this.state;
	}
	
	@Override
	public boolean test(CachedBlockPosition cachedFluidPosition_1)
	{
		FluidState blockState_1 = ((CachedFluidPosition) cachedFluidPosition_1).getFluidState();
		if(blockState_1.getFluid() != this.state.getFluid())
		{
			return false;
		}
		else
		{
			Iterator<Property<?>> var3 = this.properties.iterator();
			
			while(var3.hasNext())
			{
				Property<?> property_1 = var3.next();
				if(blockState_1.get(property_1) != this.state.get(property_1))
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	public boolean setFluidState(ServerWorld serverWorld_1, BlockPos blockPos_1, int int_1)
	{
		return StateUtils.setFluidStateInWorld(serverWorld_1, blockPos_1, this.state, int_1);
	}
}
