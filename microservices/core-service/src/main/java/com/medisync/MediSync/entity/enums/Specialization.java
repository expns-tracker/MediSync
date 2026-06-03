package com.medisync.MediSync.entity.enums;

import lombok.Getter;

@Getter
public enum Specialization {
    GENERAL_PRACTICE("General Practice"),
    CARDIOLOGY("Cardiology"),
    PEDIATRICS("Pediatrics"),
    ORTHOPEDICS("Orthopedics"),
    DERMATOLOGY("Dermatology"),
    NEUROLOGY("Neurology"),
    GYNECOLOGY("Gynecology"),
    PSYCHIATRY("Psychiatry"),
    ONCOLOGY("Oncology"),
    RADIOLOGY("Radiology"),
    OPHTHALMOLOGY("Ophthalmology"),
    DENTISTRY("Dentistry"),
    SURGERY("Surgery"),
    UROLOGY("Urology"),
    ENT("Ear, Nose, and Throat");

    private final String label;

    Specialization(String label) {
        this.label = label;
    }

}
