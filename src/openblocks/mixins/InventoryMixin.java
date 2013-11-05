package openblocks.mixins;

import openblocks.asm.mixins.ExtensionPoint;
import openblocks.common.GenericInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class InventoryMixin implements IInventory {
	protected abstract GenericInventory getInventory();

	//public abstract void onInventoryChanged();
	
	@Override
	public int getSizeInventory() {
		return getInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return getInventory().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return getInventory().decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getInventory().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		getInventory().setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() {
		return getInventory().getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return getInventory().isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() {
		return getInventory().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return getInventory().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return getInventory().isItemValidForSlot(i, itemstack);
	}
	
	@ExtensionPoint
	public void writeToNBT(NBTTagCompound tag) {
		getInventory().writeToNBT(tag);
	}

	@ExtensionPoint
	public void readFromNBT(NBTTagCompound tag) {
		getInventory().readFromNBT(tag);
	}
}
