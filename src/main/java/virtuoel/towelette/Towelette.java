package virtuoel.towelette;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import virtuoel.towelette.api.ToweletteApi;
import virtuoel.towelette.command.arguments.FluidArgumentType;
import virtuoel.towelette.command.arguments.FluidPredicateArgumentType;
import virtuoel.towelette.server.command.SetFluidCommand;
import virtuoel.towelette.util.StateUtils;

public class Towelette implements ModInitializer
{
	public static final String MOD_ID = "towelette";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static final Tag<Block> DISPLACEABLE = TagRegistry.block(id("displaceable"));
	public static final Tag<Block> UNDISPLACEABLE = TagRegistry.block(id("undisplaceable"));
	
	public static boolean isLayerView(ChunkSection section, int x, int y, int z, FluidState state)
	{
		return true;
	}
	
	public static boolean ignoreBlockStateFluids(BlockState state)
	{
		return false;
	}
	
	@Override
	public void onInitialize()
	{
		ArgumentTypes.register("fluid_state", FluidArgumentType.class, new ConstantArgumentSerializer<FluidArgumentType>(FluidArgumentType::create));
		ArgumentTypes.register("fluid_predicate", FluidPredicateArgumentType.class, new ConstantArgumentSerializer<FluidPredicateArgumentType>(FluidPredicateArgumentType::create));
		
		CommandRegistry.INSTANCE.register(false, commandDispatcher ->
		{
			commandDispatcher.register(CommandManager.literal("getfluid").requires(commandSource ->
			{
				return commandSource.hasPermissionLevel(2);
			}).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) ->
			{
				BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
				FluidState state = context.getSource().getWorld().getFluidState(pos);
				context.getSource().sendFeedback(new LiteralText(StateUtils.serializeFluidState(state).toString()), true);
				return 1;
			})));
			
			commandDispatcher.register(CommandManager.literal("getblock").requires(commandSource ->
			{
				return commandSource.hasPermissionLevel(2);
			}).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) ->
			{
				BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
				BlockState state = context.getSource().getWorld().getBlockState(pos);
				context.getSource().sendFeedback(new LiteralText(TagHelper.serializeBlockState(state).toString()), true);
				return 1;
			})));
			
			SetFluidCommand.register(commandDispatcher);
		});
	}
	
	public static Identifier id(final String name)
	{
		return new Identifier(ToweletteApi.MOD_ID, name);
	}
}
