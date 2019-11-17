package virtuoel.towelette;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.Reflection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.serialize.ArgumentSerializer;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.command.CommandManager;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.api.ToweletteApi;
import virtuoel.towelette.command.arguments.StateArgumentType;
import virtuoel.towelette.server.command.SetStateCommand;

public class Towelette implements ModInitializer
{
	public static final Logger LOGGER = LogManager.getLogger(ToweletteApi.MOD_ID);
	
	public static final Tag<Block> DISPLACEABLE = TagRegistry.block(id("displaceable"));
	public static final Tag<Block> UNDISPLACEABLE = TagRegistry.block(id("undisplaceable"));
	
	public static boolean ignoreBlockStateFluids(BlockState state)
	{
		return false;
	}
	
	@Override
	public void onInitialize()
	{
		Reflection.initialize(
			PaletteRegistrar.class,
			ChunkSection.class
		);
		
		PaletteRegistrar.PALETTES.add(PaletteRegistrar.FLUID_STATE,
			PaletteData.<Fluid, FluidState>builder()
			.ids(Fluid.STATE_IDS)
			.deserializer(PaletteRegistrar::deserializeFluidState)
			.serializer(PaletteRegistrar::serializeFluidState)
			.emptyPredicate(FluidState::isEmpty)
			.lightUpdatePredicate(PaletteRegistrar::shouldUpdateFluidStateLight)
			.stateAdditionCallback(PaletteRegistrar::onFluidStateAdded)
			.stateNeighborUpdateCallback(PaletteRegistrar::onFluidStateNeighborUpdate)
			.registry(Registry.FLUID)
			.entryFunction(FluidState::getFluid)
			.defaultStateFunction(Fluid::getDefaultState)
			.managerFunction(Fluid::getStateFactory)
			.emptyStateSupplier(Fluids.EMPTY::getDefaultState)
			.build()
		);
		
		@SuppressWarnings("rawtypes") final ArgumentSerializer<StateArgumentType> stateSerializer = new ConstantArgumentSerializer<StateArgumentType>(StateArgumentType::create);
		ArgumentTypes.register("state", StateArgumentType.class, stateSerializer);
	//	ArgumentTypes.register("state_predicate", StatePredicateArgumentType.class, new ConstantArgumentSerializer<StatePredicateArgumentType>(StatePredicateArgumentType::create));
		
		CommandRegistry.INSTANCE.register(false, commandDispatcher ->
		{
			commandDispatcher.register(CommandManager.literal("getfluid").requires(commandSource ->
			{
				return commandSource.hasPermissionLevel(2);
			}).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) ->
			{
				BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
				FluidState state = context.getSource().getWorld().getFluidState(pos);
				context.getSource().sendFeedback(new LiteralText(PaletteRegistrar.serializeFluidState(state).toString()), true);
				return 1;
			})));
			
			commandDispatcher.register(CommandManager.literal("getblock").requires(commandSource ->
			{
				return commandSource.hasPermissionLevel(2);
			}).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) ->
			{
				BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
				BlockState state = context.getSource().getWorld().getBlockState(pos);
				context.getSource().sendFeedback(new LiteralText(PaletteRegistrar.serializeBlockState(state).toString()), true);
				return 1;
			})));
			
			SetStateCommand.register(commandDispatcher);
		});
	}
	
	public static void registerBlockPaletteData(final Palette<BlockState> palette)
	{
		PaletteRegistrar.PALETTES.add(PaletteRegistrar.BLOCK_STATE,
			PaletteData.<Block, BlockState>builder()
			.palette(palette)
			.ids(Block.STATE_IDS)
			.deserializer(PaletteRegistrar::deserializeBlockState)
			.serializer(PaletteRegistrar::serializeBlockState)
			.emptyPredicate(BlockState::isAir)
			.invalidPositionSupplier(Blocks.VOID_AIR::getDefaultState)
			.lightUpdatePredicate(PaletteRegistrar::shouldUpdateBlockStateLight)
			.heightmapCallback(PaletteRegistrar::blockStateHeightmapUpdate)
			.stateAdditionCallback(PaletteRegistrar::onBlockStateAdded)
			.stateNeighborUpdateCallback(PaletteRegistrar::onBlockStateNeighborUpdate)
			.registry(Registry.BLOCK)
			.entryFunction(BlockState::getBlock)
			.defaultStateFunction(Block::getDefaultState)
			.managerFunction(Block::getStateFactory)
			.emptyStateSupplier(Blocks.AIR::getDefaultState)
			.build()
		);
	}
	
	public static Identifier id(final String name)
	{
		return new Identifier(ToweletteApi.MOD_ID, name);
	}
}
