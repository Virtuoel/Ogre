package virtuoel.towelette.command.arguments;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.CachedStatePosition;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.LayerData;

public class StateArgument<O, S extends State<S>> implements Predicate<CachedBlockPosition>
{
	private final S state;
	private final Set<Property<?>> properties;
	
	private LayerData<O, S> layer;
	
	public StateArgument(LayerData<O, S> layer, S state, Set<Property<?>> properties)
	{
		this.layer = layer;
		this.state = state;
		this.properties = properties;
	}
	
	public LayerData<O, S> getLayer()
	{
		return this.layer;
	}
	
	public S getState()
	{
		return this.state;
	}
	
	@Override
	public boolean test(CachedBlockPosition pos)
	{
		final S state = ((CachedStatePosition) pos).getState(layer);
		
		final O entry = layer.getOwner(this.state);
		final O otherEntry = layer.getOwner(state);
		
		if (otherEntry != entry)
		{
			return false;
		}
		else
		{
			for (Property<?> property : this.properties)
			{
				if (state.get(property) != this.state.get(property))
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	public boolean setState(ServerWorld world, BlockPos pos, int flags)
	{
		return ((ModifiableWorldStateLayer) world).setState(this.layer, pos, this.state, flags);
	}
}
