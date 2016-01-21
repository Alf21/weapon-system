package me.alf21.weaponsystem;

import net.gtaun.shoebill.common.player.PlayerLifecycleObject;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.object.Textdraw;
import net.gtaun.shoebill.object.Timer;
import net.gtaun.util.event.EventManager;

import java.util.ArrayList;

/**
 * Created by Alf21 on 28.04.2015 in project weapon_system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 **/

class PlayerData extends PlayerLifecycleObject {
    private Player player;
    private int money;
	private boolean isNoDM;
	private float health;
	private String playerStatus;
	private int currentWeapon;
	private int brandObjects;
	private boolean createBrand = true;
	private Timer timer;
	private Timer playerTimer;
	private Textdraw weaponStatusText;
	private int initializingWeapon;
	private int holdingKey;
	private ArrayList<Timer> timerList;
	private int count;

    public PlayerData(EventManager eventManager, Player player) {
        super(eventManager, player);
        this.player = player;
        timerList = new ArrayList<Timer>();
    }

    public Player getPlayer() {
        return player;
    }

    int getMoney() {
        return money;
    }

    void setMoney(int money) {
        this.money = money;
    }
    
    boolean isNoDM() {
		return isNoDM;
	}
    
    float getHealth(){
    	return health;
    }
    
    void setHealth(float health){
    	this.health = health;
    }
    
    String getPlayerStatus() {
		return playerStatus;
	}
    
    void setPlayerStatus(String playerStatus) {
		this.playerStatus = playerStatus;
	}
    
    int getCurrentWeapon() {
		return currentWeapon;
	}
    
    void setCurrentWeapon(int currentWeapon) {
		this.currentWeapon = currentWeapon;
	}
    
    int getBrandObjects() {
		return brandObjects;
	}
    
    void setBrandObjects(int brandObjects) {
		this.brandObjects = brandObjects;
	}
    
    boolean getCreateBrand(){
    	return createBrand;
    }
    
    void setCreateBrand(boolean createBrand) {
		this.createBrand = createBrand;
	}
    
    Timer getPlayerTimer() {
    	return playerTimer;
	}
    
    void setPlayerTimer(Timer playerTimer) {
		this.playerTimer = playerTimer;
	}
    
    Textdraw getWeaponStatusText() {
		return weaponStatusText;
	}
    
    void setWeaponStatusText(Textdraw weaponStatusText) {
		this.weaponStatusText = weaponStatusText;
	}
    
    int getInitializingWeapon() {
		return initializingWeapon;
	}
    
    void setInitializingWeapon(int initializingWeapon) {
		this.initializingWeapon = initializingWeapon;
	}
    
    int getHoldingKey() {
		return holdingKey;
	}
    
    void setHoldingKey(int holdingKey) {
		this.holdingKey = holdingKey;
	}
    
    public int getCount() {
		return count;
	}
    public void setCount(int count) {
		this.count = count;
	}
    
    public ArrayList<Timer> getTimerList() {
		return timerList;
	}
    

    @Override
    protected void onInit() {
        timer = Timer.create(PlayerManager.brandObjectsUpdateTime, (factualInterval) -> {
        	createBrand = true;
        });
        timer.start();
    }

    @Override
    protected void onDestroy() {
    	if(timer != null) timer.destroy();
    	if(playerTimer != null) playerTimer.destroy();
    	if(timerList != null && !timerList.isEmpty()) timerList.clear();
    	timerList = null;
    }
}
