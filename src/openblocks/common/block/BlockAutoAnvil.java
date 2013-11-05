package openblocks.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.Config;
import openblocks.asm.mixins.MixinPartsRegistry;
import openblocks.common.tileentity.TileEntityAutoAnvil;
import openblocks.mixins.SidedInventoryMixin;

public class BlockAutoAnvil extends OpenBlock {

	public BlockAutoAnvil() {
		super(Config.blockAutoAnvilId, Material.anvil);
		setStepSound(soundAnvilFootstep);
		setupBlock(this, "autoanvil", MixinPartsRegistry.instance.createClass(TileEntityAutoAnvil.class, SidedInventoryMixin.class));
		setRotationMode(BlockRotationMode.FOUR_DIRECTIONS);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		return false;
	}
}
