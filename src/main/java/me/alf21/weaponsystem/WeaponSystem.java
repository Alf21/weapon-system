package me.alf21.weaponsystem;

import net.gtaun.shoebill.common.player.PlayerLifecycleHolder;
import net.gtaun.shoebill.constant.WeaponModel;
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
    
//External functions
    public void givePlayerWeapon(Player player, int weaponId, int ammo){
		WeaponData weaponData = new WeaponData(player.getName(), WeaponModel.get(weaponId));
		weaponData.setFireAmmo(0);
		weaponData.setExplosiveAmmo(0);
		weaponData.setHeavyAmmo(0);
		weaponData.setSpecialAmmo(0);
		weaponData.setNormalAmmo(ammo);
		weaponData.setWeaponState("normal");

		playerManager.addWeaponData(player, weaponId, weaponData);
	}

    public void givePlayerNewWeapon(Player player, int weaponId, int ammo){ //To initialize new Weapons with Weapondata / Ammo and get the data later again
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setNormalAmmo(ammo);
		weaponData.setWeaponState("normal");
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setNormalAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_IGNITE(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setFireAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_EXPLOSIVE(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setExplosiveAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_ARMOUR_PIERCING(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setHeavyAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    public void setWeaponAmmo_SPEZIAL(Player player, int weaponId, int ammo){
    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
		weaponData.setSpecialAmmo(ammo);
		playerManager.addWeaponData(player, weaponId, weaponData);
		GIVE_PlayerWeapon(player, weaponId);
    }
    
    public int getWeaponAmmo(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
			return weaponData.getNormalAmmo();
		} else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_IGNITE(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
			return weaponData.getFireAmmo();
		} else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_EXPLOSIVE(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
			return weaponData.getExplosiveAmmo();
		} else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_ARMOUR_PIERCING(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
			return weaponData.getHeavyAmmo();
		} else {
    		return 0;
    	}
    }
    public int getWeaponAmmo_SPEZIAL(Player player, int weaponId){
    	if(playerManager.getWeaponData(player, weaponId) != null){
	    	WeaponData weaponData = playerManager.getWeaponData(player, weaponId);
			return weaponData.getSpecialAmmo();
		} else {
    		return 0;
    	}
    }
    
    private void GIVE_PlayerWeapon(Player player, int weaponId){
		playerManager.playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		playerManager.playerLifecycle.setPlayerStatus("INIT");
		playerManager.GIVE_PlayerExternWeapon(player, weaponId);
		playerManager.playerLifecycle.setPlayerStatus("INITED");
	}
}