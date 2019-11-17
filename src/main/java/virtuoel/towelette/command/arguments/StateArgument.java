package virtuoel.towelette.command.arguments;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.CachedStatePosition;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.PaletteRegistrar;

public class StateArgument<O, S extends PropertyContainer<S>> implements Predicate<CachedBlockPosition>
{
	private final Identifier layer;
	private final S state;
	private final Set<Property<?>> properties;
	
	public StateArgument(Identifier layer, S state, Set<Property<?>> properties)
	{
		this.layer = layer;
		this.state = state;
		this.properties = properties;
	}
	
	public S getState()
	{
		return this.state;
	}
	
	@Override
	public boolean test(CachedBlockPosition pos)
	{
		@SuppressWarnings("unchecked")
		final S state = ((CachedStatePosition<S>) pos).getState(layer);
		
		final O entry = PaletteRegistrar.<O, S>getPaletteData(layer).getEntry(this.state);
		final O otherEntry = PaletteRegistrar.<O, S>getPaletteData(layer).getEntry(state);
		
		if(otherEntry != entry)
		{
			return false;
		}
		else
		{
			for(Property<?> property : this.properties)
			{
				if(state.get(property) != this.state.get(property))
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean setState(ServerWorld world, BlockPos pos, int flags)
	{
		return ((ModifiableWorldStateLayer<O, S>) world).setState(this.layer, pos, this.state, flags);
	}
}
