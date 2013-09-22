package openblocks.utils;

import openblocks.common.tileentity.MetadataAccess;

import com.google.common.base.Preconditions;

import net.minecraftforge.common.ForgeDirection;

public class MetadataUtils {

	public static ForgeDirection getRotation(MetadataAccess metadata) {
		int ordinal = (metadata.get() & 3) + 2;
		ForgeDirection direction = ForgeDirection.getOrientation(ordinal);
		return direction;
	}

	public static void setRotation(MetadataAccess metadata, ForgeDirection rot) {
		int value = (rot.ordinal() - 2) & 3;
		metadata.set((metadata.get() & ~3) | value);
	}

	public static ForgeDirection getDirection(MetadataAccess metadata) {
		int ordinal = metadata.get() & 7;
		ForgeDirection direction = ForgeDirection.getOrientation(ordinal);
		return direction;
	}

	public static void setDirection(MetadataAccess metadata, ForgeDirection dir) {
		int value = dir.ordinal() & 7;
		metadata.set((metadata.get() & ~7) | value);
	}

	public static boolean getFlag(MetadataAccess metadata, int index) {
		Preconditions.checkPositionIndex(index, 4, "Invalid flag index");
		int mask = 1 << index;
		return (metadata.get() & mask) == mask;
	}

	public static void setFlag(MetadataAccess metadata, int index, boolean value) {
		Preconditions.checkPositionIndex(index, 4, "Invalid flag index");
		int mask = 1 << index;
		int b = value? mask : 0;
		metadata.set((metadata.get() & ~mask) | b);
	}
}
