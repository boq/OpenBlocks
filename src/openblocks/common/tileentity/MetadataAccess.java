package openblocks.common.tileentity;

import net.minecraft.tileentity.TileEntity;

import com.google.common.base.Preconditions;

public class MetadataAccess {
	protected int metadata; // local copy of metadata

	// weak protection against 'forgetting' data
	private boolean modified;

	public static class WorldAccess extends MetadataAccess {
		private final TileEntity te;

		public WorldAccess(TileEntity te) {
			this.te = te;
		}

		@Override
		public void write() {
			super.write();
			int currentBlockMeta = te.getBlockMetadata();
			if (currentBlockMeta != metadata) {
				te.worldObj.setBlockMetadataWithNotify(te.xCoord, te.yCoord, te.zCoord, metadata, 3);
				// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		@Override
		public void read() {
			super.read();
			metadata = te.getBlockMetadata();
		}
	}

	public void write() {
		modified = false;
	}

	public void read() {
		Preconditions.checkState(!modified, "Unflushed changes. Probably missing '.write()'!");
	}

	public int get() {
		return metadata;
	}

	public void set(int value) {
		this.metadata = value;
		modified = true;
	}
}
