package virtuoel.towelette.server.command;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.PropertyContainer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.command.arguments.LayerArgumentType;
import virtuoel.towelette.command.arguments.StateArgument;
import virtuoel.towelette.command.arguments.StateArgumentType;

public class SetStateCommand
{
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.setstate.failed"));
	
	public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1)
	{
		commandDispatcher_1.register(
			CommandManager.literal("setstate")
			.requires(source -> source.hasPermissionLevel(2))
			.then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()))
			.then(CommandManager.argument("layer", LayerArgumentType.layer()))
			.then(
				CommandManager.argument("state", StateArgumentType.create())
				.executes(SetStateCommand::run)
				.then(
					CommandManager.literal("keep")
					.executes(SetStateCommand::runKeep)
				)
				.then(
					CommandManager.literal("replace")
					.executes(SetStateCommand::run)
				)
			)
		);
	}
	
	private static <O, S extends PropertyContainer<S>> int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		return run(context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), StateArgumentType.<O, S>getArgument(context, "state"), null);
	}
	
	private static <O, S extends PropertyContainer<S>> int runKeep(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		final PaletteData<O, S> layer = LayerArgumentType.getLayerArgument(context, "layer");
		final StateArgument<O, S> state = StateArgumentType.getArgument(context, "state");
		final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
		return run(context.getSource(), pos, state, cachedPos ->
		{
			return layer.isEmpty(((ModifiableWorldStateLayer) cachedPos.getWorld()).getState(layer, cachedPos.getBlockPos()));
		});
	}
	
	private static <O, S extends PropertyContainer<S>> int run(ServerCommandSource source, BlockPos pos, StateArgument<O, S> stateArg, @Nullable Predicate<CachedBlockPosition> predicate) throws CommandSyntaxException
	{
		final ServerWorld world = source.getWorld();
		
		if(predicate != null && !predicate.test(new CachedBlockPosition(world, pos, true)))
		{
			throw FAILED_EXCEPTION.create();
		}
		else if(!stateArg.setState(world, pos, 2))
		{
			throw FAILED_EXCEPTION.create();
		}
		else
		{
		//	serverWorld_1.updateNeighbors(blockPos_1, blockArgument_1.getFluidState().getBlockState().getBlock());
			source.sendFeedback(new TranslatableText("commands.setstate.success", pos.getX(), pos.getY(), pos.getZ()), true);
			return 1;
		}
	}
}
