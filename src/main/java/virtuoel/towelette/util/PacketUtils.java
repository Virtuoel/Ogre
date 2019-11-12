package virtuoel.towelette.util;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.Towelette;

public class PacketUtils
{
	public static final Identifier UPDATE = Towelette.id("fluid_update");
	public static final Identifier DELTA = Towelette.id("delta_update");
	
	public static CustomPayloadS2CPacket fluidUpdate(BlockView world, BlockPos pos)
	{
		final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos);
		buffer.writeVarInt(Fluid.STATE_IDS.getId(world.getFluidState(pos)));
		return new CustomPayloadS2CPacket(UPDATE, buffer);
	}
	
	public static CustomPayloadS2CPacket deltaUpdate(int records, short[] positions, WorldChunk chunk)
	{
		return new CustomPayloadS2CPacket(DELTA, writeDelta(new PacketByteBuf(Unpooled.buffer()), records, positions, chunk));
	}
	
	private static PacketByteBuf writeDelta(PacketByteBuf buffer, int records, short[] positions, WorldChunk chunk)
	{
		final ChunkPos chunkPos = chunk.getPos();
		
		buffer.writeInt(chunkPos.x);
		buffer.writeInt(chunkPos.z);
		buffer.writeVarInt(records);
		
		for (int i = 0; i < records; ++i)
		{
			buffer.writeShort(positions[i]);
			buffer.writeVarInt(Fluid.STATE_IDS.getId(chunk.getFluidState(toBlockPos(chunkPos, positions[i]))));
		}
		
		return buffer;
	}
	
	private static BlockPos toBlockPos(ChunkPos chunkPos, short pos)
	{
		return new BlockPos(chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15));
	}
}
