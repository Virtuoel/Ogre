package virtuoel.towelette.server.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.PropertyContainer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class GetStateCommand
{
	public static <O, S extends PropertyContainer<S>> void register(LayerData<O, S> layer, CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(
			CommandManager.literal("getstate")
			.requires(commandSource -> commandSource.hasPermissionLevel(2))
			.then(
				CommandManager.argument("pos", BlockPosArgumentType.blockPos())
				.then(
					CommandManager.literal(LayerRegistrar.LAYERS.getId(layer).toString())
					.executes(context ->
					{
						final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
						final S state = ((BlockViewStateLayer) context.getSource().getWorld()).getState(layer, pos);
						context.getSource().sendFeedback(new LiteralText(layer.serializeState(state).toString()), true);
						return 1;
					})
				)
			)
		);
	}
}
