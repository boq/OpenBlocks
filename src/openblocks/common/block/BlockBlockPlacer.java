package openblocks.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import openblocks.Config;
import openblocks.asm.mixins.MixinPartsRegistry;
import openblocks.common.tileentity.TileEntityBlockPlacer;
import openblocks.mixins.SidedInventoryMixin;
import openblocks.utils.BlockUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBlockPlacer extends OpenBlock {
	@SideOnly(Side.CLIENT)
	private Icon faceIcon;

	public BlockBlockPlacer() {
		super(Config.blockBlockPlacerId, Material.rock);
		setupBlock(this, "blockPlacer", MixinPartsRegistry.instance.createClass(TileEntityBlockPlacer.class, SidedInventoryMixin.class));
		setRotationMode(BlockRotationMode.SIX_DIRECTIONS);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister registry) {
		super.registerIcons(registry);
		this.faceIcon = registry.registerIcon(String.format("%s:%s", modKey, "blockPlacer_face"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata) {
		ForgeDirection rot = BlockUtils.get3dBlockRotationFromMetadata(metadata);
		return side == rot.ordinal()? faceIcon : blockIcon;
	}
}
