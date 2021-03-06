package me.alf21.weaponsystem;

import net.gtaun.shoebill.common.player.PlayerLifecycleHolder;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.resource.Plugin;
import net.gtaun.util.event.EventManager;
import net.gtaun.util.event.EventManagerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class WeaponSystem extends Plugin {

	public static final Logger LOGGER = LoggerFactory.getLogger(WeaponSystem.class);
	private static WeaponSystem instance;
	private PlayerManager playerManager;
	private EventManager eventManager;
    private PlayerLifecycleHolder playerLifecycleHolder;
    private EventManagerNode eventManagerNode;
    private MysqlConnection mysqlConnection;

	public static WeaponSystem getInstance() {
		if (instance == null)
			instance = new WeaponSystem();
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
    
    PlayerManager getPlayerManager() {
        return playerManager;
    }
    
//External functions
    public void givePlayerWeapon(Player player, int weaponId, int ammo){
		WeaponData weaponData = new WeaponData(player.getName(), weaponId);
		weaponData.setFireAmmo(0);
		weaponData.setExplosiveAmmo(0);
		weaponData.setHeavyAmmo(0);
		weaponData.setSpecialAmmo(0);
		weaponData.setNormalAmmo(ammo);
		weaponData.setAmmoState(AmmoState.NORMAL);
		weaponData.setAble(true);

		playerManager.addWeaponData(player, weaponId, weaponData);
		
		playerManager.playerLifecycle.setPlayerStatus("INIT");
		playerManager.givePlayerExternWeapon(player, weaponId);
		playerManager.playerLifecycle.setPlayerStatus("INITED");
	}

    public void givePlayerNewWeapon(Player player, int weaponId, int ammo){ //To initialize new Weapons with Weapondata / Ammo and get the data later again
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setNormalAmmo(weaponData.getNormalAmmo() + ammo);
		weaponData.setAmmoState(AmmoState.NORMAL);
		weaponData.setAble(true);
		
		playerManager.addWeaponData(player, weaponId, weaponData);

		playerManager.playerLifecycle.setPlayerStatus("INIT");
		playerManager.givePlayerExternWeapon(player, weaponId);
		playerManager.playerLifecycle.setPlayerStatus("INITED");
    }
    public void setWeaponAmmo(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setNormalAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setFireWeaponAmmo(Player player, int weaponId, int ammo) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setFireAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setExplosiveWeaponAmmo(Player player, int weaponId, int ammo) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setExplosiveAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setHeavyWeaponAmmo(Player player, int weaponId, int ammo) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setHeavyAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setSpecialWeaponAmmo(Player player, int weaponId, int ammo) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setSpecialAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setSelectedWeapon(Player player, int weaponId, boolean bool) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setSelected(bool);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }
	
	public void unselectWeapons(Player player, int slot){
		playerManager.unselectWeapons(player, slot, "slot");
	}

	public void setAbleWeapon(Player player, int weaponId, boolean bool) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setAble(bool);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }

	public void setAmmoState(Player player, Integer weaponId, AmmoState ammoState) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setAmmoState(ammoState);
		playerManager.addWeaponData(player, weaponId, weaponData);
    }
    
    public int getWeaponAmmo(Player player, int weaponId){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getNormalAmmo();
    }

	public int getFireWeaponAmmo(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getFireAmmo();
    }

	public int getExplosiveWeaponAmmo(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getExplosiveAmmo();
    }

	public int getHeavyWeaponAmmo(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getHeavyAmmo();
    }

	public int getSpecialWeaponAmmo(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getSpecialAmmo();
    }

	public boolean isSelectedWeapon(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.isSelected();
    }

	public boolean isAbleWeapon(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.isAble();
    }

	public AmmoState getAmmoState(Player player, int weaponId) {
		WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		return weaponData.getAmmoState();
    }
}
