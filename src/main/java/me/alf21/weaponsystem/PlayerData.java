package me.alf21.weaponsystem;

import net.gtaun.shoebill.common.player.PlayerLifecycleObject;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.object.Textdraw;
import net.gtaun.util.event.EventManager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
	private HashMap<Timer, Boolean> animationReady;
	private boolean createBrand = true;
	private Timer timer = new Timer();
	private Timer playerTimer;
	private Textdraw weaponStatusText;
	private int initializingWeapon;
	private int holdingKey;
	private int animationIndex;

    public PlayerData(EventManager eventManager, Player player) {
        super(eventManager, player);
        this.player = player;
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
    
    boolean getAnimationReady(Timer timer) {
    	if(animationReady == null){
    		animationReady = new HashMap<Timer, Boolean>();
    		return true;
    	}
    	if(animationReady.containsKey(timer)){
    		return animationReady.get(timer);
    	}
    	return false;
	}
    
    void setAnimationReady(Timer timer, Boolean bool) {
    	if(animationReady == null) animationReady = new HashMap<Timer, Boolean>();
    	animationReady.put(timer, bool);
	}
    
    boolean getCreateBrand(){
    	return createBrand;
    }
    
    void setCreateBrand(boolean createBrand) {
		this.createBrand = createBrand;
	}
    
    Timer getPlayerTimer() {
    	if(playerTimer == null){
    		playerTimer = new Timer();
    		return playerTimer;
    	}
    	else return playerTimer;
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
    
    int getAnimationIndex() {
		return animationIndex;
	}
    
    void setAnimationIndex(int animationIndex) {
		this.animationIndex = animationIndex;
	}
    

    @Override
    protected void onInit() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
    			createBrand = true;
            }
        }, 0, PlayerManager.brandObjectsUpdateTime);
    }

    @Override
    protected void onDestroy() {
    	timer.cancel();
    }
}
