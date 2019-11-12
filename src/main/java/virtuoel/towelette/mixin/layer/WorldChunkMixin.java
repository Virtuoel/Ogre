package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.ChunkFluidLayer;
import virtuoel.towelette.api.ChunkSectionFluidLayer;
import virtuoel.towelette.api.UpdateableFluid;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ChunkFluidLayer
{
	@Override
	public FluidState setFluidState(BlockPos pos, FluidState state)
	{
		final WorldChunk self = (WorldChunk) (Object) this;
		final World world = self.getWorld();
		
		int x = pos.getX() & 15;
		int y = pos.getY();
		int z = pos.getZ() & 15;
		
		ChunkSection[] sections = self.getSectionArray();
		
		ChunkSection section = sections[y >> 4];
		if(section == WorldChunk.EMPTY_SECTION)
		{
			if(state.isEmpty())
			{
				return null;
			}
			
			section = new ChunkSection(y >> 4 << 4);
			sections[y >> 4] = section;
		}
		
		boolean wasEmpty = section.isEmpty();
		FluidState oldState = ((ChunkSectionFluidLayer) section).setFluidState(x, y & 15, z, state);
		if(oldState == state)
		{
			return null;
		}
		else
		{
			Fluid fluid = state.getFluid();
			self.getHeightmap(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, state.getBlockState());
			self.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, state.getBlockState());
			self.getHeightmap(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, state.getBlockState());
			self.getHeightmap(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, state.getBlockState());
			boolean isEmpty = section.isEmpty();
			if(wasEmpty != isEmpty)
			{
				world.getChunkManager().getLightingProvider().updateSectionStatus(pos, isEmpty);
			}
			
			if(section.getFluidState(x, y & 15, z).getFluid() != fluid)
			{
				return null;
			}
			else
			{
				if (!world.isClient)
				{
					((UpdateableFluid) state.getFluid()).onFluidAdded(state, world, pos, oldState);
				}
				
				self.setShouldSave(true);
				return oldState;
			}
		}
	}
}
