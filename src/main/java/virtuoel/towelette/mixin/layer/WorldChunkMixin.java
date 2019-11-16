package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ChunkStateLayer
{
	@Override
	public <O, S extends PropertyContainer<S>> S getState(Identifier layer, int x, int y, int z)
	{
		final WorldChunk self = (WorldChunk) (Object) this;
		
		final ChunkSection[] sections = self.getSectionArray();
		
		try
		{
			if (y >= 0 && y >> 4 < sections.length)
			{
				ChunkSection section = sections[y >> 4];
				if (!ChunkSection.isEmpty(section))
				{
					return ((ChunkSectionStateLayer) section).getState(layer, x & 15, y & 15, z & 15);
				}
			}
			
			return PaletteRegistrar.<O, S>getPaletteData(layer).getEmptyState();
		}
		catch (Throwable t)
		{
			final CrashReport report = CrashReport.create(t, "Getting state from layer " + layer.toString());
			final CrashReportSection section = report.addElement("State being got");
			section.add("Location", () ->
			{
				return CrashReportSection.createPositionString(x, y, z);
			});
			throw new CrashException(report);
		}
	}
	
	@Override
	public <O, S extends PropertyContainer<S>> S setState(Identifier layer, BlockPos pos, S state, boolean unknownStateBoolean0912)
	{
		final WorldChunk self = (WorldChunk) (Object) this;
		final World world = self.getWorld();
		
		final int x = pos.getX() & 15;
		final int y = pos.getY();
		final int z = pos.getZ() & 15;
		
		final ChunkSection[] sections = self.getSectionArray();
		
		final PaletteData<O, S> data = PaletteRegistrar.getPaletteData(layer);
		
		ChunkSection section = sections[y >> 4];
		if(section == WorldChunk.EMPTY_SECTION)
		{
			if(data.isEmpty(state))
			{
				return null;
			}
			
			section = new ChunkSection(y >> 4 << 4);
			sections[y >> 4] = section;
		}
		
		final boolean wasEmpty = section.isEmpty();
		final S oldState = ((ChunkSectionStateLayer) section).setState(layer, x, y & 15, z, state);
		if(oldState == state)
		{
			return null;
		}
		else
		{
			final O entry = data.getEntry(state);
	//	*	self.getHeightmap(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, state.getBlockState());
	//	*	self.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, state.getBlockState());
	//	*	self.getHeightmap(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, state.getBlockState());
	//	*	self.getHeightmap(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, state.getBlockState());
			final boolean isEmpty = section.isEmpty();
			if(wasEmpty != isEmpty)
			{
				world.getChunkManager().getLightingProvider().updateSectionStatus(pos, isEmpty);
			}
			
			if(data.getEntry(((ChunkSectionStateLayer) section).<O, S>getState(layer, x, y & 15, z)) != entry)
			{
				return null;
			}
			else
			{
				if (!world.isClient)
				{
	//				((UpdateableFluid) state.getFluid()).onFluidAdded(state, world, pos, oldState);
				}
				
				self.setShouldSave(true);
				return oldState;
			}
		}
	}
}
