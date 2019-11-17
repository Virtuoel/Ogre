package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;

public interface CachedStatePosition<S extends PropertyContainer<S>>
{
	S getState(Identifier layer);
}
