package openblocks.client.renderer;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.Log;
import openblocks.OpenBlocks;
import openblocks.client.renderer.tileentity.OpenRenderHelper;
import openblocks.common.tileentity.*;
import openblocks.utils.MetadataUtils;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockRenderingHandler implements ISimpleBlockRenderingHandler {

	private final Map<Block, TileEntity> inventoryTileEntities = Maps.newIdentityHashMap();

	private void addRenderer(Block target, OpenTileEntity te, MetadataAccess meta) {
		te.setMeta(meta);
		inventoryTileEntities.put(target, te);
	}
	
	private void addRenderer(Block target, OpenTileEntity te) {
		addRenderer(target, te, new MetadataAccess());
	}
	
	private void addRenderer(Block target, TileEntity te) {
		inventoryTileEntities.put(target, te);
	}
	
	public BlockRenderingHandler() {		
		addRenderer(OpenBlocks.Blocks.lightbox, new TileEntityLightbox());
		addRenderer(OpenBlocks.Blocks.grave, new TileEntityGrave());
		addRenderer(OpenBlocks.Blocks.trophy, new TileEntityTrophy());
		addRenderer(OpenBlocks.Blocks.sprinkler, new TileEntitySprinkler());
		addRenderer(OpenBlocks.Blocks.vacuumHopper, new TileEntityVacuumHopper());
		addRenderer(OpenBlocks.Blocks.bigButton,  new TileEntityBigButton());
		addRenderer(OpenBlocks.Blocks.fan, new TileEntityFan());
		
		{
			TileEntityCannon te = new TileEntityCannon();
			te.disableLineRender();
			addRenderer(OpenBlocks.Blocks.cannon, te);
		}
		
		{
			TileEntityBearTrap te = new TileEntityBearTrap();
			te.setOpen();
			addRenderer(OpenBlocks.Blocks.bearTrap, te);
		}
		
		{
			MetadataAccess fakeMeta = new MetadataAccess();
			MetadataUtils.setRotation(fakeMeta, ForgeDirection.WEST);
			MetadataUtils.setFlag(fakeMeta, TileEntityTarget.FLAG_ENABLED, true);
			fakeMeta.write();
			addRenderer(OpenBlocks.Blocks.target, new TileEntityTarget(), fakeMeta);
		}

		{
			MetadataAccess fakeMeta = new MetadataAccess();
			MetadataUtils.setFlag(fakeMeta, TileEntityFlag.FLAG_ON_GROUND, true);
			fakeMeta.write();
			addRenderer(OpenBlocks.Blocks.flag,new TileEntityFlag(), fakeMeta);
		}
	}

	@Override
	public int getRenderId() {
		return OpenBlocks.renderId;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {

		TileEntity te = inventoryTileEntities.get(block);

		if (te instanceof OpenTileEntity) {
			((OpenTileEntity)te).prepareForInventoryRender(block, metadata);
		}

		try {
			final World world = Minecraft.getMinecraft().theWorld;
			if (world != null) {
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
				if (te != null) {
					te.worldObj = world;
					GL11.glTranslated(-0.5, -0.5, -0.5);
					TileEntityRenderer.instance.renderTileEntityAt(te, 0.0D, 0.0D, 0.0D, 0.0F);
				} else {
					OpenRenderHelper.renderCube(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5, block, null);
				}
			}
		} catch (Exception e) {
			Log.severe(e, "Error during block '%s' rendering", block.getUnlocalizedName());
			Throwables.propagate(e);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

}
