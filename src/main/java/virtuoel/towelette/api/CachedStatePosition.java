package virtuoel.towelette.api;

import net.minecraft.util.Identifier;

public interface CachedStatePosition
{
	<S> S getState(Identifier layer);
}
