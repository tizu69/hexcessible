package dev.tizu.hexcessible.smartsig;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class HexicalMacro implements SmartSig.Conditional {

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("hexical");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        return getAllMacros().stream().map(m -> getFor(Utils.angle(m))).toList();
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var all = getAllMacros();
        for (var macro : all) {
            if (macro.equals(Utils.angle(sig)))
                return getFor(sig);
        }
        return null;
    }

    private List<String> getAllMacros() {
        var player = Minecraft.getInstance().player;
        var inventory = player.getInventory();

        var targetItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hexical", "grimoire"));
        var stacks = Stream.of(inventory.items, inventory.offhand, inventory.armor,
                player.getEnderChestInventory().items)
                .flatMap(Collection::stream)
                .filter(stack -> stack.is(targetItem))
                .toList();
        // return stacks.stream()
        //         .map(ItemStack::getNbt)
        //         .filter(Objects::nonNull)
        //         .map(nbt -> nbt.getCompound("expansions"))
        //         .flatMap(nbt -> nbt.getKeys().stream())
        //         .toList();
        // TODO: Hexical is not ported yet from what I can tell
        return List.of();
    }

    private PatternEntries.Entry getFor(List<HexAngle> sig) {
        var i18nkey = Component.translatable("hexcessible.smartsig.grimoire").getString();
        return new PatternEntries.Entry("hexical:grimoire_macro/" + Utils.angle(sig),
                i18nkey, () -> false, HexDir.EAST, List.of(sig), List.of(), 0);
    }
}
