package me.shedaniel.materialisation.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import me.shedaniel.materialisation.ModReference;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class ResettableSimpleRegistry<T> implements MutableRegistry<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected Int2ObjectBiMap<T> indexedEntries = Int2ObjectBiMap.create(256);
    protected BiMap<Identifier, T> entries = HashBiMap.create();
    private BiMap<RegistryKey<T>, T> entriesByKey = HashBiMap.create();
    private Set<RegistryKey<T>> loadedKeys = Sets.newIdentityHashSet();
    protected Object[] randomEntries;
    private int nextId;
    private String id;
    private RegistryKey<Registry<T>> registryKey;
    private Lifecycle lifecycle;

    public ResettableSimpleRegistry(String id) {
        super();
        this.id = id;
        this.registryKey = RegistryKey.ofRegistry(Identifier.of(ModReference.MOD_ID, id));
        this.lifecycle = Lifecycle.stable();
    }

    @SuppressWarnings("unused")
    public ResettableSimpleRegistry(RegistryKey<Registry<T>> registryKey, Lifecycle lifecycle) {
        super();
        this.registryKey = registryKey;
        this.lifecycle = lifecycle;
    }
    
    public void reset() {
        indexedEntries = Int2ObjectBiMap.create(256);
        entries = HashBiMap.create();
        entriesByKey = HashBiMap.create();
        loadedKeys = Sets.newIdentityHashSet();
        randomEntries = null;
        nextId = 0;
    }



    @Override
    public RegistryEntry.Reference<T> add(RegistryKey<T> key, T entry, RegistryEntryInfo info) {
        Validate.notNull(key);
        Validate.notNull(entry);
        int rawId = nextId;
        this.indexedEntries.put(entry, rawId);

        this.randomEntries = null;
        if (this.entriesByKey.containsKey(key)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", key);
        }

        this.entries.put(key.getValue(), entry);
        this.entriesByKey.put(key, entry);
        if (this.nextId <= rawId) {
            this.nextId = rawId + 1;
        }

        return RegistryEntry.Reference.standAlone(this.getEntryOwner(), key);
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Nullable
    @Override
    public RegistryEntryLookup<T> createMutableEntryLookup() {
        return null;
    }

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return registryKey;
    }

    @Nullable
    public Identifier getId(T entry) {
        return this.entries.inverse().get(entry);
    }

    public Optional<RegistryKey<T>> getKey(T value) {
        return Optional.ofNullable(this.entriesByKey.inverse().get(value));
    }
    
    public int getRawId(@Nullable T entry) {
        return this.indexedEntries.getRawId(entry);
    }
    
    @Nullable
    public T get(@Nullable RegistryKey<T> key) {
        return this.entriesByKey.get(key);
    }
    
    @Nullable
    public T get(int index) {
        return this.indexedEntries.get(index);
    }

    @Override
    public int size() {
        return 0;
    }

    public @NotNull Iterator<T> iterator() {
        return this.indexedEntries.iterator();
    }
    
    @Nullable
    public T get(@Nullable Identifier id) {
        return this.entries.get(id);
    }

    @Override
    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> key) {
        return Optional.empty();
    }

    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public Optional<T> getOrEmpty(@Nullable Identifier id) {
        return Optional.ofNullable(this.entries.get(id));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getDefaultEntry() {
        return Optional.empty();
    }

    public Set<Identifier> getIds() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Collections.unmodifiableSet(this.entriesByKey.entrySet());
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return Collections.unmodifiableSet(this.entriesByKey.keySet());
    }

    @Nullable
    @SuppressWarnings("unused")
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        if (this.randomEntries == null) {
            Collection<T> collection = this.entries.values();
            if (collection.isEmpty()) {
                return Optional.empty();
            }
            
            this.randomEntries = collection.toArray(new Object[0]);
        }

        //noinspection unchecked
        return Optional.of((RegistryEntry.Reference<T>) RegistryEntry.of((T) Util.getRandom(this.randomEntries, random)));
    }
    
    public boolean containsId(Identifier id) {
        return this.entries.containsKey(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return false;
    }

    @Override
    public Registry<T> freeze() {
        return null;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return Optional.empty();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier id) {
        return Optional.empty();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
        return Optional.empty();
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        return null;
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return null;
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.empty();
    }

    @Override
    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return null;
    }

    @Override
    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return null;
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return null;
    }

    @Override
    public void clearTags() {

    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {

    }

    @Nullable
    @Override
    public RegistryEntryOwner<T> getEntryOwner() {
        return null;
    }

    @Nullable
    @Override
    public RegistryWrapper.Impl<T> getReadOnlyWrapper() {
        return null;
    }

    @SuppressWarnings("unused")
    public boolean isLoaded(RegistryKey<T> registryKey) {
        return this.loadedKeys.contains(registryKey);
    }

    @SuppressWarnings("unused")
    public void markLoaded(RegistryKey<T> registryKey) {
        this.loadedKeys.add(registryKey);
    }

}