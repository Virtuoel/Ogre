package virtuoel.towelette.util;

import java.util.Arrays;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.Direction;

public class StateNeighborGroup
{
	private final Direction direction;
	private final PropertyContainer<?>[] states;
	
	public StateNeighborGroup(Direction direction, PropertyContainer<?>... states)
	{
		this.direction = direction;
		this.states = states;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (!(obj instanceof StateNeighborGroup))
		{
			return false;
		}
		else
		{
			final StateNeighborGroup other = (StateNeighborGroup) obj;
			return this.direction == other.direction && Arrays.equals(this.states, other.states);
		}
	}
	
	@Override
	public int hashCode()
	{
		return this.direction.hashCode() + Arrays.hashCode(states);
	}
}
