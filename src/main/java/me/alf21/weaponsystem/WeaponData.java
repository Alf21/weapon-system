package me.alf21.weaponsystem;

import net.gtaun.shoebill.constant.WeaponModel;

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
	private String weaponState;
	private String playerName;
	private WeaponModel weaponModel;
	//private HashMap<String, Integer> muni_LOADED;

	public WeaponData(String playerName, WeaponModel weaponModel) {
		this.playerName = playerName;
		this.weaponModel = weaponModel;
	}


	public WeaponModel getWeaponModel() {
		return weaponModel;
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

    /*
    HashMap<String, Integer> getMUNI_Loaded() {
		return muni_LOADED;
	}
    
    void setMUNI_Loaded(HashMap<String, Integer> muni_LOADED) {
		this.muni_LOADED = muni_LOADED;
	}
	*/

	String getWeaponState() {
		return weaponState;
	}

	void setWeaponState(String weaponState) {
		this.weaponState = weaponState;
	}
}
