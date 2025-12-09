package com.cc.DTO;

import java.util.List;

/**
 * DTO for returning locations grouped by state with districts
 */
public class StateLocationDTO {

    private String state;
    private List<DistrictDTO> districts;

    public StateLocationDTO() {
    }

    public StateLocationDTO(String state, List<DistrictDTO> districts) {
        this.state = state;
        this.districts = districts;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<DistrictDTO> getDistricts() {
        return districts;
    }

    public void setDistricts(List<DistrictDTO> districts) {
        this.districts = districts;
    }

    /**
     * Inner class for district information
     */
    public static class DistrictDTO {
        private Long id;
        private String district;
        private boolean isWholeState; // Special flag for "Whole State" option

        public DistrictDTO() {
        }

        public DistrictDTO(Long id, String district, boolean isWholeState) {
            this.id = id;
            this.district = district;
            this.isWholeState = isWholeState;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public boolean isWholeState() {
            return isWholeState;
        }

        public void setWholeState(boolean isWholeState) {
            this.isWholeState = isWholeState;
        }
    }
}
