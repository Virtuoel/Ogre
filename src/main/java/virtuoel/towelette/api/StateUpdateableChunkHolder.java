package virtuoel.towelette.api;

import net.minecraft.util.Identifier;

public interface StateUpdateableChunkHolder
{
	void markForStateUpdate(Identifier layer, int x, int y, int z);
}
