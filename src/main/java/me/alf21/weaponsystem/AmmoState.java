package me.alf21.weaponsystem;

/**
 * Created by marvin on 15.05.15 in project weapon_system.
 * Copyright (c) 2015 Marvin Haschker. All rights reserved.
 */
public enum AmmoState {
    NORMAL("Normal"),
    FIRE("Brand"),
    EXPLOSIVE("Explosiv"),
    HEAVY("Panzerbrechend"),
    SPECIAL("Speziell");

    private String displayName;

    AmmoState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
