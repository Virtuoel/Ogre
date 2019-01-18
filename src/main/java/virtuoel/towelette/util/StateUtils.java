package virtuoel.towelette.util;

import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.ChunkSectionFluidLayer;

public class StateUtils
{
	public static boolean setFluidStateInWorld(World world, BlockPos blockPos_1, FluidState state, int int_1)
	{
		if(World.isHeightInvalid(blockPos_1))
		{
			return false;
		}
		else if(!world.isClient && world.getLevelProperties().getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES)
		{
			return false;
		}
		else
		{
			WorldChunk worldChunk_1 = world.getWorldChunk(blockPos_1);
		//	Fluid block_1 = state.getFluid();
			FluidState blockState_2 = setFluidStateInChunk(worldChunk_1, blockPos_1, state, (int_1 & 64) != 0);
			if(blockState_2 == null)
			{
				return false;
			}
			else
			{
				FluidState blockState_3 = world.getFluidState(blockPos_1);
				if(blockState_3 != blockState_2 && (blockState_3.getBlockState().getLightSubtracted(world, blockPos_1) != blockState_2.getBlockState().getLightSubtracted(world, blockPos_1) || blockState_3.getBlockState().getLuminance() != blockState_2.getBlockState().getLuminance() || blockState_3.getBlockState().hasSidedTransparency() || blockState_2.getBlockState().hasSidedTransparency()))
				{
				//	world.profiler.push("queueCheckLight");
					world.getChunkManager().getLightingProvider().enqueueLightUpdate(blockPos_1);
				//	world.profiler.pop();
				}
				
				if(blockState_3 == state)
				{
					if(blockState_2 != blockState_3)
					{
						world.scheduleBlockRender(blockPos_1, Blocks.AIR.getDefaultState(), Blocks.VOID_AIR.getDefaultState());
					}
					/*
					if((int_1 & 2) != 0 && (!world.isClient || (int_1 & 4) == 0) && (world.isClient || worldChunk_1.method_12225() != null && worldChunk_1.method_12225().method_14014(ServerChunkManagerEntry.class_3194.TICKING)))
					{
						world.updateListeners(blockPos_1, blockState_2, state, int_1);
					}
					
					if(!world.isClient && (int_1 & 1) != 0)
					{
						world.updateNeighbors(blockPos_1, blockState_2.getBlock());
						if(state.hasComparatorOutput())
						{
							world.updateHorizontalAdjacent(blockPos_1, block_1);
						}
					}
					
					if((int_1 & 16) == 0)
					{
						int int_2 = int_1 & -2;
						blockState_2.method_11637(world, blockPos_1, int_2);
						state.updateNeighborStates(world, blockPos_1, int_2);
						state.method_11637(world, blockPos_1, int_2);
					}*/
				}
				 // TODO FIXME Evaluate ^ this
				
				
				return true;
			}
		}
	}
	
	public static FluidState setFluidStateInChunk(WorldChunk chunk, BlockPos blockPos_1, FluidState state, boolean boolean_1)
	{
		int int_1 = blockPos_1.getX() & 15;
		int int_2 = blockPos_1.getY();
		int int_3 = blockPos_1.getZ() & 15;
		
		World world = chunk.getWorld();
		ChunkSection[] sections = chunk.getSectionArray();
		
		ChunkSection chunkSection_1 = sections[int_2 >> 4];
		if(chunkSection_1 == WorldChunk.EMPTY_SECTION)
		{
			if(state.isEmpty())
			{
				return null;
			}
			
			chunkSection_1 = new ChunkSection(int_2 >> 4 << 4);
			sections[int_2 >> 4] = chunkSection_1;
		}
		
		boolean boolean_2 = chunkSection_1.isEmpty();
		FluidState fluidState = ((ChunkSectionFluidLayer) chunkSection_1).setFluidState(int_1, int_2 & 15, int_3, state);
		if(fluidState == state)
		{
			return null;
		}
		else
		{
			Fluid fluid_1 = state.getFluid();
		//	Fluid fluid_2 = fluidState.getFluid();
		//	((Heightmap) this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(int_1, int_2, int_3, blockState_1);
		//	((Heightmap) this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(int_1, int_2, int_3, blockState_1);
		//	((Heightmap) this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(int_1, int_2, int_3, blockState_1);
		//	((Heightmap) this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(int_1, int_2, int_3, blockState_1);
			boolean boolean_3 = chunkSection_1.isEmpty();
			if(boolean_2 != boolean_3)
			{
				world.getChunkManager().getLightingProvider().updateSectionStatus(blockPos_1, boolean_3);
			}
			
			if(chunkSection_1.getFluidState(int_1, int_2 & 15, int_3).getFluid() != fluid_1)
			{
				return null;
			}
			else
			{
				chunk.markDirty();
				return fluidState;
			}
		}
	}
	
	public static FluidState deserializeFluidState(CompoundTag compound)
	{
		if(!compound.containsKey("Name", 8))
		{
			return Fluids.EMPTY.getDefaultState();
		}
		else
		{
			Fluid fluid = Registry.FLUID.get(new Identifier(compound.getString("Name")));
			FluidState state = fluid.getDefaultState();
			if(compound.containsKey("Properties", 10))
			{
				CompoundTag properties = compound.getCompound("Properties");
				StateFactory<Fluid, FluidState> stateFactory = fluid.getStateFactory();
				
				for(String key : properties.getKeys())
				{
					Property<?> property = stateFactory.getProperty(key);
					if(property != null)
					{
						state = withProperty(state, property, key, properties, compound);
					}
				}
			}
			
			return state;
		}
	}
	
	private static <S extends PropertyContainer<S>, T extends Comparable<T>> S withProperty(S container, Property<T> property, String key, CompoundTag properties, CompoundTag compound)
	{
		Optional<T> value = property.getValue(properties.getString(key));
		if(value.isPresent())
		{
			return container.with(property, value.get());
		}
		else
		{
			Towelette.LOGGER.warn("Unable to read property: {} with value: {} for property container: {}", key, properties.getString(key), compound.toString());
			return container;
		}
	}
	
	public static CompoundTag serializeFluidState(FluidState state)
	{
		CompoundTag stateCompound = new CompoundTag();
		stateCompound.putString("Name", Registry.FLUID.getId(state.getFluid()).toString());
		ImmutableMap<Property<?>, Comparable<?>> entries = state.getEntries();
		if(!entries.isEmpty())
		{
			CompoundTag propertyCompound = new CompoundTag();
			
			for(Entry<Property<?>, Comparable<?>> entry : entries.entrySet())
			{
				@SuppressWarnings("rawtypes")
				Property property = entry.getKey();
				@SuppressWarnings("unchecked")
				String name = property.getName(entry.getValue());
				propertyCompound.putString(property.getName(), name);
			}
			
			stateCompound.put("Properties", propertyCompound);
		}
		
		return stateCompound;
	}
}
