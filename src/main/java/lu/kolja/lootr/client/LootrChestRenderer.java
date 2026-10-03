package lu.kolja.lootr.client;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import lu.kolja.lootr.Lootr;
import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.block.LootrChestBlock;
import lu.kolja.lootr.tile.LootrChestTileEntity;

public class LootrChestRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation NORMAL = new ResourceLocation("textures/entity/chest/normal.png");
    private static final ResourceLocation TRAPPED = new ResourceLocation("textures/entity/chest/trapped.png");
    private static final ResourceLocation CUSTOM_UNOPENED = new ResourceLocation(Lootr.MODID, "textures/chest.png");
    private static final ResourceLocation CUSTOM_OPENED = new ResourceLocation(
        Lootr.MODID,
        "textures/chest_opened.png");

    private final ModelChest model = new ModelChest();
    private Boolean customTextures;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        var tile = (LootrChestTileEntity) te;
        int meta = tile.hasWorldObj() ? tile.getBlockMetadata() : 3;
        boolean trapped = (meta & LootrChestBlock.TRAPPED_BIT) != 0;
        boolean opened = isOpened(tile);
        int tint = 0xFFFFFF;

        if (!LootrConfig.vanillaTextures && hasCustomTextures()) {
            bindTexture(opened ? CUSTOM_OPENED : CUSTOM_UNOPENED);
        } else {
            bindTexture(trapped ? TRAPPED : NORMAL);
            tint = opened ? LootrConfig.openedTint : LootrConfig.unopenedTint;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f((tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F, (tint & 255) / 255.0F, 1.0F);
        GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(rotation(LootrChestBlock.facing(meta)), 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        float lid = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTicks;
        lid = 1.0F - lid;
        lid = 1.0F - lid * lid * lid;
        model.chestLid.rotateAngleX = -(lid * (float) Math.PI / 2.0F);
        model.renderAll();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static float rotation(int facing) {
        return switch (facing) {
            case 2 -> 180.0F;
            case 4 -> 90.0F;
            case 5 -> -90.0F;
            default -> 0.0F;
        };
    }

    private static boolean isOpened(LootrChestTileEntity tile) {
        var player = Minecraft.getMinecraft().thePlayer;
        return player != null && tile.getOpeners()
            .contains(player.getUniqueID());
    }

    private boolean hasCustomTextures() {
        if (customTextures == null) {
            customTextures = exists(CUSTOM_UNOPENED) && exists(CUSTOM_OPENED);
        }
        return customTextures;
    }

    private static boolean exists(ResourceLocation location) {
        try {
            Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(location);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
