package lu.kolja.lootr.mixins.early;

import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import lu.kolja.lootr.worldgen.WorldgenTracker;

@Mixin(ChunkProviderServer.class)
public class MixinChunkProviderServer {

    @WrapMethod(method = "populate")
    private void lootr$trackPopulate(IChunkProvider provider, int chunkX, int chunkZ, Operation<Void> original) {
        WorldgenTracker.beginPopulate();
        try {
            original.call(provider, chunkX, chunkZ);
        } finally {
            WorldgenTracker.endPopulate();
        }
    }
}
