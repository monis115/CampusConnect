package com.cc.controller;

import com.cc.modals.Location;
import com.cc.repository.LocationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    // ✅ 1. Get all locations
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return ResponseEntity.ok(locations);
    }

    // ✅ 1b. Get all locations grouped by state with "Whole State" option
    @GetMapping("/grouped")
    public ResponseEntity<List<com.cc.DTO.StateLocationDTO>> getGroupedLocations() {
        List<Location> allLocations = locationRepository.findAll();

        // Group by state
        java.util.Map<String, List<Location>> locationsByState = allLocations.stream()
                .collect(java.util.stream.Collectors.groupingBy(Location::getState));

        List<com.cc.DTO.StateLocationDTO> result = new java.util.ArrayList<>();

        // Sort states alphabetically
        List<String> sortedStates = new java.util.ArrayList<>(locationsByState.keySet());
        java.util.Collections.sort(sortedStates);

        for (String state : sortedStates) {
            List<Location> stateLocations = locationsByState.get(state);

            List<com.cc.DTO.StateLocationDTO.DistrictDTO> districts = new java.util.ArrayList<>();

            // Add "Whole State" option first (id = -1 or null, frontend handles this)
            districts.add(new com.cc.DTO.StateLocationDTO.DistrictDTO(null, "Whole " + state, true));

            // Add individual districts sorted alphabetically
            stateLocations.stream()
                    .sorted((a, b) -> a.getDistrict().compareToIgnoreCase(b.getDistrict()))
                    .forEach(loc -> districts.add(
                            new com.cc.DTO.StateLocationDTO.DistrictDTO(loc.getId(), loc.getDistrict(), false)));

            result.add(new com.cc.DTO.StateLocationDTO(state, districts));
        }

        return ResponseEntity.ok(result);
    }

    // ✅ 2. Get locations by state (for dropdown filtering)
    @GetMapping("/state/{state}")
    public ResponseEntity<List<Location>> getLocationsByState(@PathVariable String state) {
        List<Location> locations = locationRepository.findByStateIgnoreCase(state);
        return ResponseEntity.ok(locations);
    }

    // ✅ 3. Add a new location (state + district)
    @PostMapping
    public ResponseEntity<Location> addLocation(@RequestBody Location location) {
        if (location.getState() == null || location.getDistrict() == null) {
            return ResponseEntity.badRequest().build();
        }
        Location savedLocation = locationRepository.save(location);
        return ResponseEntity.ok(savedLocation);
    }

    // ✅ 4. Update existing location
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location updatedLocation) {
        Optional<Location> existing = locationRepository.findById(id);
        if (existing.isPresent()) {
            Location loc = existing.get();
            loc.setState(updatedLocation.getState());
            loc.setDistrict(updatedLocation.getDistrict());
            Location saved = locationRepository.save(loc);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ 5. Delete a location
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ 6. Bulk Add Locations (Seeding)
    @PostMapping("/bulk")
    public ResponseEntity<?> addLocationsBulk(@RequestBody List<Location> locations) {
        int addedCount = 0;
        int skippedCount = 0;

        for (Location loc : locations) {
            if (loc.getState() != null && loc.getDistrict() != null) {
                if (!locationRepository.existsByStateAndDistrict(loc.getState(), loc.getDistrict())) {
                    locationRepository.save(loc);
                    addedCount++;
                } else {
                    skippedCount++;
                }
            }
        }

        return ResponseEntity.ok(java.util.Map.of(
                "message", "Bulk processing complete",
                "added", addedCount,
                "skipped", skippedCount));
    }
}
