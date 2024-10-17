package com.example.jail;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

public class Jail {
   private final String name;
   private final Location location;
   private final int capacity;
   private Location unjailLocation;
   private final Set<String> jailedPlayers = new HashSet<>();

   public Jail(String name, Location location, int capacity, Location unjailLocation) {
      this.name = name;
      this.location = location;
      this.capacity = capacity;
      this.unjailLocation = unjailLocation;
   }

   public String getName() {
      return name;
   }

   public Location getLocation() {
      return location;
   }

   public int getCapacity() {
      return capacity;
   }

   public boolean isFull() {
      return jailedPlayers.size() >= capacity;
   }

   public Set<String> getJailedPlayers() {
      return new HashSet<>(jailedPlayers);
   }

   public void addPlayer(String playerName) {
      if (playerName != null) {
         jailedPlayers.add(playerName);
      }
   }

   public void removePlayer(String playerName) {
      if (playerName != null) {
         jailedPlayers.remove(playerName);
      }
   }

   public Location getUnjailLocation() {
      return unjailLocation;
   }

   public void setUnjailLocation(Location unjailLocation) {
      this.unjailLocation = unjailLocation;
   }
}
