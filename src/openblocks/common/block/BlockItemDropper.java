package openblocks.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import openblocks.Config;
import openblocks.asm.mixins.MixinPartsRegistry;
import openblocks.common.tileentity.TileEntityItemDropper;
import openblocks.mixins.InventoryMixin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockItemDropper extends OpenBlock {
	@SideOnly(Side.CLIENT)
	private Icon downIcon;

	public BlockItemDropper() {
		super(Config.blockItemDropperId, Material.rock);
		setupBlock(this, "itemDropper", MixinPartsRegistry.instance.createClass(TileEntityItemDropper.class, InventoryMixin.class));
		setRotationMode(BlockRotationMode.SIX_DIRECTIONS);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister registry) {
		super.registerIcons(registry);
		this.downIcon = registry.registerIcon(String.format("%s:%s", modKey, "itemDropper_down"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata) {
		switch (side) {
			case 0:
				return downIcon;
			default:
				return blockIcon;
		}
	}
}
