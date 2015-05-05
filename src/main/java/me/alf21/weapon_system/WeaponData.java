package me.alf21.weapon_system;

//import java.util.HashMap;

import net.gtaun.shoebill.object.Destroyable;

/**
 * Created by Alf21 on 28.04.2015 in project weapon_system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 **/

class WeaponData implements Destroyable {
	private int muni_EXPLOSIV;
	private int muni_BRAND;
	private int muni_NORMAL;
	private int muni_PANZERBRECHEND;
	private int muni_SPEZIELL;
	private String weaponStatus;
	private String playername;
	private int weaponId;
	//private HashMap<String, Integer> muni_LOADED;

    public WeaponData(String playername, int weaponId) {
        this.playername = playername;
        this.weaponId = weaponId;
    }

    
    public int getWeaponId(){
    	return weaponId;
    }
    public String getPlayername(){
    	return playername;
    }
    
    int getMUNI_Explosiv() {
		return muni_EXPLOSIV;
	}
    
    void setMUNI_Explosiv(int muni_EXPLOSIV) {
		this.muni_EXPLOSIV = muni_EXPLOSIV;
	}
    
    int getMUNI_Normal() {
		return muni_NORMAL;
	}
    
    void setMUNI_Normal(int muni_NORMAL) {
		this.muni_NORMAL = muni_NORMAL;
	}
    
    int getMUNI_Panzerbrechend() {
		return muni_PANZERBRECHEND;
	}
    
    void setMUNI_Panzerbrechend(int muni_PANZERBRECHEND) {
		this.muni_PANZERBRECHEND = muni_PANZERBRECHEND;
	}
    
    int getMUNI_Brand() {
		return muni_BRAND;
	}
    
    void setMUNI_Brand(int muni_BRAND) {
		this.muni_BRAND = muni_BRAND;
	}
    
    int getMUNI_Speziell() {
		return muni_SPEZIELL;
	}
    
    void setMUNI_Speziell(int muni_SPEZIELL) {
		this.muni_SPEZIELL = muni_SPEZIELL;
	}
    
    /*
    HashMap<String, Integer> getMUNI_Loaded() {
		return muni_LOADED;
	}
    
    void setMUNI_Loaded(HashMap<String, Integer> muni_LOADED) {
		this.muni_LOADED = muni_LOADED;
	}
	*/
    
    String getWeaponStatus() {
		return weaponStatus;
	}
    
    void setWeaponStatus(String weaponStatus) {
		this.weaponStatus = weaponStatus;
	}


	@Override
	public void destroy() {
		
	}


	@Override
	public boolean isDestroyed() {
		return true;
	}
}
