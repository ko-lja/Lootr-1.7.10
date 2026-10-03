package lu.kolja.lootr.mixins.early;

import java.util.Random;

import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lu.kolja.lootr.worldgen.LootCategories;

@Mixin(value = ChestGenHooks.class, remap = false)
public class MixinChestGenHooks {

    @Shadow
    private String category;

    @Inject(method = "getItems(Ljava/util/Random;)[Lnet/minecraft/util/WeightedRandomChestContent;", at = @At("RETURN"))
    private void lootr$rememberCategory(Random rnd, CallbackInfoReturnable<WeightedRandomChestContent[]> cir) {
        LootCategories.remember(cir.getReturnValue(), category);
    }
}
