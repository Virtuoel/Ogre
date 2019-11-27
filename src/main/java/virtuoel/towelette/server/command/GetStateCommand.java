package virtuoel.towelette.server.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.PropertyContainer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class GetStateCommand
{
	public static <O, S extends PropertyContainer<S>> void register(LayerData<O, S> layer, CommandDispatcher<ServerCommandSource> dispatcher)
	{
		final Identifier layerId = LayerRegistrar.LAYERS.getId(layer);
		dispatcher.register(
			CommandManager.literal("getstate")
			.requires(commandSource -> commandSource.hasPermissionLevel(2))
			.then(
				CommandManager.argument("pos", BlockPosArgumentType.blockPos())
				.then(
					CommandManager.literal(layerId.toString())
					.executes(context ->
					{
						final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
						final S state = ((BlockViewStateLayer) context.getSource().getWorld()).getState(layer, pos);
						
						final Text response = new LiteralText("");
						response.append(new LiteralText(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ())).setStyle(new Style().setColor(Formatting.YELLOW)));
						response.append("\n");
						response.append(new LiteralText(layerId.toString()).setStyle(new Style().setColor(Formatting.DARK_GREEN)));
						response.append("\n");
						response.append(layer.serializeState(state).toText());
						context.getSource().sendFeedback(response, true);
						return 1;
					})
				)
			)
		);
	}
}
