package openblocks.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import openblocks.OpenBlocks;
import openblocks.OpenBlocks.Gui;
import openblocks.common.GenericInventory;
import openblocks.common.api.IAwareTile;
import openblocks.common.api.ISurfaceAttachment;
import openblocks.utils.MetadataUtils;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityBigButton extends OpenTileEntity implements IAwareTile, ISurfaceAttachment, IInventory {

	private static final int FLAG_ACTIVE = 2;

	private int tickCounter = 0;

	private GenericInventory inventory = new GenericInventory("bigbutton", true, 1);

	public boolean isActive() {
		return MetadataUtils.getFlag(readMetadata(), FLAG_ACTIVE);
	}
	
	private void changeState(boolean state) {
		MetadataAccess meta = readMetadata();
		MetadataUtils.setFlag(meta, FLAG_ACTIVE, state);
		meta.write();
		
		ForgeDirection rot = MetadataUtils.getRotation(meta);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, OpenBlocks.Blocks.bigButton.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord + rot.offsetX, yCoord + rot.offsetY, zCoord + rot.offsetZ, OpenBlocks.Blocks.bigButton.blockID);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!worldObj.isRemote) {
			if (tickCounter > 0) {
				tickCounter--;
				if (tickCounter <= 0) {
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "random.click", 0.3F, 0.5F);
					changeState(false);
				}
			}
		}
	}

	public int getTickTime() {
		ItemStack stack = inventory.getStackInSlot(0);
		return stack == null ? 1 : stack.stackSize;
	}

	@Override
	public void onBlockBroken() {}

	@Override
	public void onBlockAdded() {}

	@Override
	public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!worldObj.isRemote) {
			if (player.isSneaking()) {
				openGui(player, Gui.BigButton);
			} else {
				tickCounter = getTickTime();
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "random.click", 0.3F, 0.6F);
				changeState(true);
			}
		}
		return true;
	}

	@Override
	public void onNeighbourChanged(int blockId) {}

	@Override
	public void onBlockPlacedBy(EntityPlayer player, ForgeDirection side, ItemStack stack, float hitX, float hitY, float hitZ) {
		setRotation(side.getOpposite());
	}

	@Override
	public boolean onBlockEventReceived(int eventId, int eventParam) {
		return false;
	}

	@Override
	public ForgeDirection getSurfaceDirection() {
		return getRotation();
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return inventory.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return inventory.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inventory.setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() {
		return inventory.getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return inventory.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return inventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return inventory.isItemValidForSlot(i, itemstack);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		inventory.writeToNBT(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		inventory.readFromNBT(tag);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void prepareForInventoryRender(Block block, int metadata) {
		super.prepareForInventoryRender(block, metadata);
		GL11.glTranslated(-0.5, 0, 0);
	}

}
