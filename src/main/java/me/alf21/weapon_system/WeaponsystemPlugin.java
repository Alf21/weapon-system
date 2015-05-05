package me.alf21.weapon_system;

import net.gtaun.shoebill.common.player.PlayerLifecycleHolder;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.resource.Plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gtaun.util.event.EventManager;
import net.gtaun.util.event.EventManagerNode;

/**
 * Created by Alf21 on 28.04.2015 in project weapon_system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 * http://forum.sa-mp.de/index.php?page=VCard&userID=34293
 * 							or
 * search for Alf21 in http://sa-mp.de || Breadfish
 * 
 * My website:
 * 				http://gsk.bplaced.net
 **/

public class WeaponsystemPlugin extends Plugin {

	public static final Logger LOGGER = LoggerFactory.getLogger(WeaponsystemPlugin.class);
	private PlayerManager playerManager;
	private static WeaponsystemPlugin instance;
	private EventManager eventManager;
    private PlayerLifecycleHolder playerLifecycleHolder;
    private EventManagerNode eventManagerNode;
    private MysqlConnection mysqlConnection;

    public static WeaponsystemPlugin getInstance() {
        if(instance == null)
            instance = new WeaponsystemPlugin();
        return instance;
    }
	
	@Override
	protected void onDisable() throws Throwable {
		playerLifecycleHolder.destroy();
        eventManagerNode.destroy();
		playerManager.uninitialize();
		playerManager.destroy();
		playerManager = null;
        mysqlConnection.closeConnection();
	}

	@Override
	protected void onEnable() throws Throwable {
		instance = this;
		eventManager = getEventManager();
        eventManagerNode = eventManager.createChildNode();
        playerLifecycleHolder = new PlayerLifecycleHolder(eventManager);
        playerLifecycleHolder.registerClass(PlayerData.class);
		playerManager = new PlayerManager();
        mysqlConnection = new MysqlConnection();
        mysqlConnection.initConnection();
        mysqlConnection.makeDatabase();
	}

    Logger getLoggerInstance() {
        return LOGGER;
    }

    EventManager getEventManagerInstance() {
        return eventManagerNode;
    }
    
    PlayerLifecycleHolder getPlayerLifecycleHolder() {
        return playerLifecycleHolder;
    }
    
    MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }
    
//External functions
    public void givePlayerWeapon(Player player, int weaponId, int ammo){
    	WeaponData weaponData = new WeaponData(player.getName(), weaponId);
    	weaponData.setMUNI_Brand(0);
    	weaponData.setMUNI_Explosiv(0);
    	weaponData.setMUNI_Panzerbrechend(0);
    	weaponData.setMUNI_Speziell(0);
    	weaponData.setMUNI_Normal(ammo);
    	weaponData.setWeaponStatus("normal");
    	
    	playerManager.addWeaponData(player, weaponId, weaponData);
    }
    public void givePlayerNewWeapon(Player player, int weaponId, int ammo){ //To initialize new Weapons with Weapondata / Ammo and get the data later again
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Normal(ammo);
    	weaponData.setWeaponStatus("normal");
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Normal(ammo);
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_IGNITE(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Brand(ammo);
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_EXPLOSIVE(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Explosiv(ammo);
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_ARMOUR_PIERCING(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Panzerbrechend(ammo);
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_SPEZIAL(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
    	weaponData.setMUNI_Speziell(ammo);
    	playerManager.addWeaponData(player, weaponId, weaponData);
    	GIVE_PlayerWeapon(player, weaponId);
    }
    
    public int getWeaponAmmo(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
	    	return weaponData.getMUNI_Normal();
    	}
    	else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_IGNITE(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
	    	return weaponData.getMUNI_Brand();
    	}
    	else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_EXPLOSIVE(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
	    	return weaponData.getMUNI_Explosiv();
    	}
    	else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_ARMOUR_PIERCING(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
	    	return weaponData.getMUNI_Panzerbrechend();
    	}
    	else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_SPEZIAL(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
	    	return weaponData.getMUNI_Speziell();
    	}
    	else {
    		return 0;
    	}
    }
    
    private void GIVE_PlayerWeapon(Player player, int weaponId){
	    playerManager.playerLifecycle = WeaponsystemPlugin.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
	    playerManager.playerLifecycle.setPlayerStatus("INIT");
		playerManager.GIVE_PlayerExternWeapon(player, weaponId);
		playerManager.playerLifecycle.setPlayerStatus("INITED");
	}
}
