package lu.kolja.lootr.mixins.early;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lu.kolja.lootr.worldgen.WorldgenTracker;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "setTileEntity", at = @At("HEAD"))
    private void lootr$recordWorldgenTile(int x, int y, int z, TileEntity tileEntityIn, CallbackInfo ci) {
        WorldgenTracker.onTileEntitySet((World) (Object) this, x, y, z, tileEntityIn);
    }
}
