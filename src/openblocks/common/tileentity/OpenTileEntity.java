package openblocks.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.OpenBlocks;
import openblocks.common.block.OpenBlock;
import openblocks.common.tileentity.MetadataAccess.WorldAccess;
import openblocks.utils.MetadataUtils;

import com.google.common.base.Preconditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class OpenTileEntity extends TileEntity {

	private boolean updated = false;

	private MetadataAccess meta;

	public OpenTileEntity() {}

	// use this to inject fake data for rendering
	public void setMeta(MetadataAccess meta) {
		Preconditions.checkNotNull(meta);
		this.meta = meta;
	}

	public MetadataAccess readMetadata() {
		if (meta == null)
		// nobody set anything? Then we probably aren't rendering now
		meta = new WorldAccess(this);

		// make sure we have current value
		meta.read();
		return meta;
	}

	@SideOnly(Side.CLIENT)
	public void prepareForInventoryRender(Block block, int metadata) {}

	@Override
	public void updateEntity() {
		if (!updated) {
			firstUpdate();
			updated = true;
		}
	}

	public boolean isAddedToWorld() {
		return worldObj != null;
	}

	protected void firstUpdate() {}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}

	public TileEntity getTileInDirection(ForgeDirection direction) {
		int x = xCoord + direction.offsetX;
		int y = yCoord + direction.offsetY;
		int z = zCoord + direction.offsetZ;
		if (worldObj != null && worldObj.blockExists(x, y, z)) { return worldObj.getBlockTileEntity(x, y, z); }
		return null;
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s", xCoord, yCoord, zCoord);
	}

	public boolean isAirBlock(ForgeDirection direction) {
		return worldObj != null
				&& worldObj.isAirBlock(xCoord + direction.offsetX, yCoord
						+ direction.offsetY, zCoord + direction.offsetZ);
	}

	public void sendBlockEvent(int key, int value) {
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord), key, value);
	}

	@Override
	public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldID != newID;
	}

	public OpenBlock getBlock() {
		Block block = Block.blocksList[worldObj.getBlockId(xCoord, yCoord, zCoord)];
		if (block instanceof OpenBlock) { return (OpenBlock)block; }
		return null;
	}

	public void openGui(EntityPlayer player, Enum<?> gui) {
		player.openGui(OpenBlocks.instance, gui.ordinal(), worldObj, xCoord, yCoord, zCoord);
	}

	public AxisAlignedBB getBB() {
		return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	// most common metadata configuration
	protected void setRotation(ForgeDirection rotation) {
		MetadataAccess meta = readMetadata();
		MetadataUtils.setRotation(meta, rotation);
		meta.write();
	}

	public ForgeDirection getRotation() {
		return MetadataUtils.getRotation(readMetadata());
	}
}
