package me.shedaniel.materialisation;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class MaterialisationComponentTypes {
    public static ComponentType<NbtComponent> MATERIALISED_DATA;

    public static void register() {
        MATERIALISED_DATA = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(ModReference.MOD_ID, "materialised_data"), ComponentType.<NbtComponent>builder().codec(NbtComponent.CODEC).build());
    }
}
