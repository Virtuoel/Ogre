package virtuoel.towelette.server.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.State;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class GetStatesCommand
{
	@SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(
			CommandManager.literal("getstates")
			.requires(commandSource -> commandSource.hasPermissionLevel(2))
			.then(
				CommandManager.argument("pos", BlockPosArgumentType.blockPos())
				.executes(context ->
				{
					final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
					
					final Text response = new LiteralText("");
					response.append(new LiteralText(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ())).setStyle(new Style().setColor(Formatting.YELLOW)));
					for (@SuppressWarnings("rawtypes") LayerData layer : LayerRegistrar.LAYERS)
					{
						final State<?> state = ((BlockViewStateLayer) context.getSource().getWorld()).getState(layer, pos);
						
						response.append("\n");
						response.append(new LiteralText(LayerRegistrar.LAYERS.getId(layer).toString()).setStyle(new Style().setColor(Formatting.DARK_GREEN)));
						response.append("\n");
						response.append(layer.serializeState(state).toText());
					}
					
					context.getSource().sendFeedback(response, true);
					return 1;
				})
			)
		);
	}
}
