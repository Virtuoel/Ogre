package virtuoel.towelette.api;

import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.PalettedContainer;

public interface ChunkSectionFluidLayer
{
	default FluidState setFluidState(int x, int y, int z, FluidState state)
	{
		return setFluidState(x, y, z, state, true);
	}
	
	FluidState setFluidState(int x, int y, int z, FluidState state, boolean synchronous);
	
	PalettedContainer<FluidState> getFluidStateContainer();
}
