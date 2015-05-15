package me.alf21.weaponsystem;

/**
 * Created by Alf21 on 28.04.2015 in project weapon_system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 **/

public class WeaponData {
	private int explosiveAmmo;
	private int fireAmmo;
	private int normalAmmo;
	private int heavyAmmo;
	private int specialAmmo;
	private AmmoState ammoState;
	private String playerName;
	private Integer weaponId;
	private boolean selected;
	private boolean able;

	public WeaponData(String playerName, Integer weaponId) {
		this.playerName = playerName;
		this.weaponId = weaponId;
	}


	public Integer getWeaponId() {
		return weaponId;
	}

	public String getPlayerName() {
		return playerName;
	}

	int getExplosiveAmmo() {
		return explosiveAmmo;
	}

	void setExplosiveAmmo(int explosiveAmmo) {
		this.explosiveAmmo = explosiveAmmo;
	}

	int getNormalAmmo() {
		return normalAmmo;
	}

	void setNormalAmmo(int normalAmmo) {
		this.normalAmmo = normalAmmo;
	}

	int getHeavyAmmo() {
		return heavyAmmo;
	}

	void setHeavyAmmo(int heavyAmmo) {
		this.heavyAmmo = heavyAmmo;
	}

	int getFireAmmo() {
		return fireAmmo;
	}

	void setFireAmmo(int fireAmmo) {
		this.fireAmmo = fireAmmo;
	}

	int getSpecialAmmo() {
		return specialAmmo;
	}

	void setSpecialAmmo(int specialAmmo) {
		this.specialAmmo = specialAmmo;
	}
	
	boolean isSelected() {
		return selected;
	}
	
	void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	boolean isAble() {
		return able;
	}
	
	void setAble(boolean able) {
		this.able = able;
	}

	AmmoState getAmmoState() {
		return ammoState;
	}

	void setAmmoState(AmmoState weaponState) {
		this.ammoState = weaponState;
	}
}
