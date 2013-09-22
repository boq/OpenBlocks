package openblocks.common.tileentity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import openblocks.OpenBlocks;
import openblocks.common.api.IAwareTile;
import openblocks.common.api.ISurfaceAttachment;
import openblocks.common.block.BlockFlag;
import openblocks.sync.ISyncableObject;
import openblocks.sync.SyncableFloat;
import openblocks.sync.SyncableInt;
import openblocks.utils.BlockUtils;
import openblocks.utils.MetadataUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityFlag extends NetworkedTileEntity implements
		ISurfaceAttachment, IAwareTile {

	public static final int FLAG_ON_GROUND = 2;

	public enum Keys {
		angle, colorIndex
	}

	private SyncableFloat angle = new SyncableFloat(0.0f);
	private SyncableInt colorIndex = new SyncableInt(0);

	public TileEntityFlag() {
		addSyncedObject(Keys.angle, angle);
		addSyncedObject(Keys.colorIndex, colorIndex);
	}

	@Override
	public void onSynced(List<ISyncableObject> changes) {}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		colorIndex.readFromNBT(tag, "color");
		angle.readFromNBT(tag, "angle");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		colorIndex.writeToNBT(tag, "color");
		angle.writeToNBT(tag, "angle");
	}

	public Icon getIcon() {
		return OpenBlocks.Blocks.flag.getIcon(0, 0);
	}

	public void setColorIndex(int index) {
		colorIndex.setValue(index);
	}

	public void setAngle(float ang) {
		angle.setValue(ang);
	}

	public boolean isOnGround() {
		return MetadataUtils.getFlag(readMetadata(), FLAG_ON_GROUND);
	}

	public int getColor() {
		if (colorIndex.getValue() >= BlockFlag.COLORS.length) colorIndex.setValue(0);
		return BlockFlag.COLORS[colorIndex.getValue()];
	}

	@Override
	public ForgeDirection getSurfaceDirection() {
		MetadataAccess meta = readMetadata();
		return MetadataUtils.getFlag(meta, FLAG_ON_GROUND) ? ForgeDirection.DOWN : MetadataUtils.getRotation(meta);
	}

	public float getAngle() {
		return angle.getValue();
	}

	@Override
	public void onBlockBroken() {}

	@Override
	public void onBlockAdded() {}

	@Override
	public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (player != null && player.isSneaking()) { return true; }
		if (!worldObj.isRemote) {
			if (getSurfaceDirection() == ForgeDirection.DOWN) {
				angle.setValue(angle.getValue() + 10f);
				sync();
				return false;
			}
		}
		return true;
	}

	@Override
	public void onNeighbourChanged(int blockId) {}

	@Override
	public void onBlockPlacedBy(EntityPlayer player, ForgeDirection side, ItemStack stack, float hitX, float hitY, float hitZ) {
		float ang = player.rotationYawHead;
		ForgeDirection surface = side.getOpposite();

		if (surface != ForgeDirection.DOWN) {
			ang = -BlockUtils.getRotationFromDirection(side.getOpposite());
		}

		setAngle(ang);
		setColorIndex(stack.getItemDamage());
		
		MetadataAccess meta = readMetadata();
		MetadataUtils.setRotation(meta, side.getOpposite());
		MetadataUtils.setFlag(meta, FLAG_ON_GROUND, surface == ForgeDirection.DOWN);
		meta.write();
		
		sync();
	}

	@Override
	public boolean onBlockEventReceived(int eventId, int eventParam) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void prepareForInventoryRender(Block block, int metadata) {
		super.prepareForInventoryRender(block, metadata);
		setColorIndex(metadata);
	}
}
