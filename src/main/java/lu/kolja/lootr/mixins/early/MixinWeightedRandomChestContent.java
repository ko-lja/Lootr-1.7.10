package lu.kolja.lootr.mixins.early;

import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.WeightedRandomChestContent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lu.kolja.lootr.worldgen.LootCategories;

@Mixin(WeightedRandomChestContent.class)
public class MixinWeightedRandomChestContent {

    @Inject(method = "generateChestContents", at = @At("HEAD"))
    private static void lootr$recordCategory(Random random, WeightedRandomChestContent[] items, IInventory inventory,
        int count, CallbackInfo ci) {
        LootCategories.onFill(items, inventory);
    }
}
