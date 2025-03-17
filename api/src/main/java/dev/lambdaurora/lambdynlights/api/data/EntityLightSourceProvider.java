package dev.lambdaurora.lambdynlights.api.data;

import dev.lambdaurora.lambdynlights.api.entity.EntityLightSource;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate entity light source JSON files via datagen.
 *
 * @author Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class EntityLightSourceProvider extends LightSourceProvider<EntityLightSource> {
    public EntityLightSourceProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider, String modId) {
        super(packOutput, registryProvider, modId, "entity", EntityLightSource.CODEC);
    }

    /**
     * Adds a new entity light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param predicate the predicate to select which entities emit the given luminance
     * @param luminances the luminance sources
     */
    protected void add(String key, EntityLightSource.EntityPredicate predicate, EntityLuminance... luminances) {
        add(key, new EntityLightSource(predicate, List.of(luminances)));
    }

    /**
     * Adds a new entity light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param type The entity type that emits the given luminance.
     * @param luminances The luminances to use.
     */
    protected void add(String key, EntityType<?> type, EntityLuminance... luminances) {
        add(key, EntityLightSource.EntityPredicate.builder().of(type).build(), luminances);
    }

    /**
     * Adds a new entity light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param tag The tag that selects the entity types that emit the given luminance.
     * @param luminances The luminances to use.
     */
    protected void add(String key, TagKey<EntityType<?>> tag, EntityLuminance... luminances) {
        add(key, EntityLightSource.EntityPredicate.builder().of(tag).build(), luminances);
    }
}
