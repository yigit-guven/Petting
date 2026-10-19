package net.yigitguven.petting.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
    private static final String DATA_NAME = "petting_pets";

    private final Map<UUID, Set<UUID>> playerPets = new HashMap<>();
    private final Map<UUID, UUID> petOwner = new HashMap<>();

    public PetSavedData() {
    }

    public static PetSavedData load(CompoundTag nbt) {
        PetSavedData data = new PetSavedData();
        ListTag entries = nbt.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("player")) {
                UUID playerUuid = entry.getUUID("player");
                ListTag petList = entry.getList("pets", Tag.TAG_INT_ARRAY);
                Set<UUID> pets = new HashSet<>();
                for (int j = 0; j < petList.size(); j++) {
                    try {
                        UUID petUuid = NbtUtils.loadUUID(petList.get(j));
                        pets.add(petUuid);
                        data.petOwner.put(petUuid, playerUuid);
                    } catch (Exception ignored) {}
                }
                data.playerPets.put(playerUuid, pets);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : playerPets.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putUUID("player", entry.getKey());
                ListTag petList = new ListTag();
                for (UUID pet : entry.getValue()) {
                    petList.add(NbtUtils.createUUID(pet));
                }
                entryTag.put("pets", petList);
                entries.add(entryTag);
            }
        }
        nbt.put("entries", entries);
        return nbt;
    }

    public static PetSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(PetSavedData::load, PetSavedData::new, DATA_NAME);
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
