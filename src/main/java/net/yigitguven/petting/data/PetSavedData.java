package net.yigitguven.petting.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PetSavedData extends SavedData {

    public static final Factory<PetSavedData> FACTORY = new Factory<>(
            PetSavedData::new,
            PetSavedData::load
    );

    private final Map<UUID, Set<UUID>> playerPets = new HashMap<>();
    private final Map<UUID, UUID> petOwner = new HashMap<>();

    public PetSavedData() {
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : playerPets.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putUUID("player", entry.getKey());
                ListTag petList = new ListTag();
                for (UUID pet : entry.getValue()) {
                    CompoundTag petTag = new CompoundTag();
                    petTag.putUUID("id", pet);
                    petList.add(petTag);
                }
                entryTag.put("pets", petList);
                listTag.add(entryTag);
            }
        }
        tag.put("entries", listTag);
        return tag;
    }

    public static PetSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PetSavedData data = new PetSavedData();
        ListTag listTag = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entryTag = listTag.getCompound(i);
            if (entryTag.hasUUID("player")) {
                UUID playerUuid = entryTag.getUUID("player");
                ListTag petList = entryTag.getList("pets", Tag.TAG_COMPOUND);
                Set<UUID> pets = new HashSet<>();
                for (int j = 0; j < petList.size(); j++) {
                    CompoundTag petTag = petList.getCompound(j);
                    if (petTag.hasUUID("id")) {
                        UUID petUuid = petTag.getUUID("id");
                        pets.add(petUuid);
                        data.petOwner.put(petUuid, playerUuid);
                    }
                }
                if (!pets.isEmpty()) {
                    data.playerPets.put(playerUuid, pets);
                }
            }
        }
        return data;
    }

    public static PetSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "petting_pets");
    }

    public int getPetCount(UUID playerUuid) {
        Set<UUID> pets = playerPets.get(playerUuid);
        return pets != null ? pets.size() : 0;
    }

    public boolean addPet(UUID playerUuid, UUID petUuid) {
        UUID previousOwner = petOwner.get(petUuid);
        if (previousOwner != null && !previousOwner.equals(playerUuid)) {
            Set<UUID> prevPets = playerPets.get(previousOwner);
            if (prevPets != null) {
                prevPets.remove(petUuid);
                if (prevPets.isEmpty()) {
                    playerPets.remove(previousOwner);
                }
            }
        }
        petOwner.put(petUuid, playerUuid);
        boolean added = playerPets.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(petUuid);
        if (added || previousOwner != null) {
            setDirty();
        }
        return added;
    }

    public boolean removePet(UUID petUuid) {
        UUID owner = petOwner.remove(petUuid);
        if (owner != null) {
            Set<UUID> pets = playerPets.get(owner);
            if (pets != null) {
                boolean removed = pets.remove(petUuid);
                if (pets.isEmpty()) {
                    playerPets.remove(owner);
                }
                setDirty();
                return removed;
            }
        }
        return false;
    }

    public Optional<UUID> getOwner(UUID petUuid) {
        return Optional.ofNullable(petOwner.get(petUuid));
    }
}
