package me.alf21.weaponsystem;

import net.gtaun.shoebill.Shoebill;
import net.gtaun.shoebill.constant.PlayerKey;
import net.gtaun.shoebill.constant.PlayerState;
import net.gtaun.shoebill.constant.TextDrawFont;
import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.event.player.*;
import net.gtaun.shoebill.object.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//import net.gtaun.shoebill.constant.SpecialAction;
//import net.gtaun.shoebill.object.Timer;

/**
 * Created by Alf21 on 28.04.2015 in project weapon-system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 **/

/*
 * TODO:
 *         
 * - Wenn man durch eine Explosion getoetet wurde, welche durch einen Schuss eines Spielers ausgeloest wurde:
 *   Lsg.: Bekommen der Position der Explosion
 *         Bekommen des Spielers, der dorthin bzw. in die Naehe geschossen hat
 *         Ihn als Killer setzen
 *         
 * - Animation, wenn man Weaponshop betritt, zB hinhocken und in ner Tasche rumkramen usw...
 */

public class PlayerManager implements Destroyable {
	public static final boolean allowMinigun = false; //false, if u want better performance bcus of spamming and calculation for each ammo on shot
	public static final int brandObjectsUpdateTime = 200; //Time when the next brandObject can create FOR EACH PLAYER !
	//Config
	private static final int changeWeaponFreezingTime = 1000; //freezingtime in miliseconds | 0, if no freeze
	private static final int maxBrandObjects = 50; // FOR EACH PLAYER !
	private static final boolean debug = true;
	public PlayerData playerLifecycle;
	private PlayerData externPlayerLifecycle;
	private Map<Player, HashMap<Integer, WeaponData>> weaponDataMap;
	private Timer globalTimer = new Timer();
	private Timer fireTimer = new Timer();
	private Timer fireTimer2 = new Timer();

	public PlayerManager()
	{
		this.weaponDataMap = new HashMap<>();

//PlayerConnectEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerConnectEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			playerLifecycle.setPlayerStatus("connected");
			playerLifecycle.setHoldingKey(0);
			playerLifecycle.setPlayerTimer(new Timer());
			playerLifecycle.setCurrentWeapon(0);

			initWeapons(e.getPlayer());
		});
		
//PlayerWeaponShotEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerWeaponShotEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			if (e.getHitPlayer() != null)
				externPlayerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getHitPlayer(), PlayerData.class);
			WeaponData weaponData = getWeaponData(e.getPlayer(), e.getPlayer().getArmedWeapon().getId());
			if (weaponData.getExplosiveAmmo() > 0 && weaponData.getAmmoState() == AmmoState.EXPLOSIVE) {
				for(Player victim : Player.getHumans()){
					victim.setHealth(100);
				}
				Shoebill.get().getSampObjectManager().getWorld().createExplosion(new Location(e.getPosition().x, e.getPosition().y, e.getPosition().z), 12, 1);
				for(Player victim : Player.getHumans()){
					PlayerData victimLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(victim, PlayerData.class);
					//TODO: y and z coordinates ! 
					if(victim.getLocation().x-e.getPosition().x>=4&&victim.getLocation().x-e.getPosition().x<=-4){
						victim.setHealth(victimLifecycle.getHealth());
					} else if(victim.getLocation().x-e.getPosition().x<=3&&victim.getLocation().x-e.getPosition().x>=-3){
						victim.setHealth(victimLifecycle.getHealth()-2);
					} else if(victim.getLocation().x-e.getPosition().x<=2&&victim.getLocation().x-e.getPosition().x>=-2){
						victim.setHealth(victimLifecycle.getHealth()-5);
					} else if(victim.getLocation().x-e.getPosition().x<=1&&victim.getLocation().x-e.getPosition().x>=-1){
						victim.setHealth(victimLifecycle.getHealth()-10);
					} else { 
						victim.setHealth(victimLifecycle.getHealth());
					}
				}
				if(e.getHitObject() != null){
					Shoebill.get().getSampObjectManager().getWorld().createExplosion(e.getHitObject().getLocation(), 12, 1);
				}
			}
			if (weaponData.getFireAmmo() > 0 && weaponData.getAmmoState() == AmmoState.FIRE) {
			//TODO: Entscheiden, ob man die Flammen nur für den Spieler oder auch für andere Spieler anzeigt.
				if(e.getHitPlayer() != null) externPlayerLifecycle.setPlayerStatus("igniting");
				else {
					if(playerLifecycle.getBrandObjects()+1 <= maxBrandObjects){ //max. 50 Flammen erstellen in 2sek!
						if (playerLifecycle.getCreateBrand()) {
							playerLifecycle.setCreateBrand(false);
							playerLifecycle.setBrandObjects(playerLifecycle.getBrandObjects()+1);
							SampObject sampObject = Shoebill.get().getSampObjectManager().createObject(18688, new Location(e.getPosition().x, e.getPosition().y, e.getPosition().z + 0.25f), e.getPosition(), 0);

							globalTimer.schedule(new TimerTask() {
			                    @Override
			                    public void run() {
									Shoebill.get().runOnSampThread(() -> {
										sampObject.destroy();
						    			playerLifecycle.setBrandObjects(playerLifecycle.getBrandObjects()-1);
			                    	});
			                    }
			                }, 2000);
						}
					}
				}
			}
			
			//TODO: Bug, wenn man muni alle hat, WeaponState sich ändert, aber StandartMuni nur 1 ist und durch Schuss 0 -> Danach keine Muni mehr in nachfolgenden States, nur davor. Deshalb klasse erstellen : reloadWeapon(Player player, WeaponData weaponData, Integer weaponId, String typ) 
			afterWeaponShot(e, weaponData);
		});
		
//PlayerGiveDamageEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerGiveDamageEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			WeaponData weaponData = getWeaponData(e.getPlayer(), e.getPlayer().getArmedWeapon().getId());
			float damage = e.getAmount();
			//TODO: NO-DM ZONES 
			if(!playerLifecycle.isNoDM()){
				if(e.getPlayer() != e.getVictim()){
					switch (weaponData.getAmmoState()) {
						case NORMAL:
							damage = (e.getAmount() / 100) * 50; //50% weniger Schaden / die Haelfte -> nur 50% Schaden
							break;
						case FIRE:
							damage = 1;
							break;
						case EXPLOSIVE:
							damage = (e.getAmount() / 100) * 10; //90% weniger Schaden -> 10% Schaden
							break;
						case HEAVY:
							damage = (e.getAmount() / 100) * 65; //35% weniger Schaden -> 65% Schaden
							break;
						case SPECIAL:
							damage = (e.getAmount() / 100) * 120; //20% mehr Schaden -> 120% Schaden
							break;
					}
				}
				//Satchel as Medipack
			}
			else {
				e.getVictim().setHealth(e.getVictim().getHealth());
				createWarnExplosion(e.getPlayer());
			}

			if (e.getPlayer().getArmour() > 0 && e.getPlayer().getArmour() >= damage && weaponData.getAmmoState() != AmmoState.HEAVY)
				e.getVictim().setArmour(e.getVictim().getArmour() - damage);
			else if (e.getPlayer().getArmour() > 0 && e.getPlayer().getArmour() < damage && weaponData.getAmmoState() != AmmoState.HEAVY) {
				float restDamage = damage - e.getPlayer().getArmour();
				e.getPlayer().setArmour(0);
				e.getPlayer().setHealth(e.getPlayer().getHealth() - restDamage);
			}
			else e.getVictim().setHealth(e.getVictim().getHealth() - damage);
		});
		
//PlayerUpdateEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerUpdateEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			playerLifecycle.setHealth(e.getPlayer().getHealth());
			
			if(playerLifecycle.getPlayerStatus().equals("igniting")){
				togglePlayerBurning(e.getPlayer(), true);
			}
			
			if(e.getPlayer().getArmedWeapon() != null){
				int currentWeapon = e.getPlayer().getArmedWeapon().getId();
				if(currentWeapon != playerLifecycle.getCurrentWeapon()){
					OnPlayerChangeWeapon(e.getPlayer(), playerLifecycle.getCurrentWeapon(), currentWeapon);
				} else { //TODO To include PAWN Weapons ! ATTENTION, no ANTI-CHEAT
					OnPlayerUpdateWeapon(e.getPlayer(), currentWeapon);
				}
				playerLifecycle.setCurrentWeapon(currentWeapon);
			}

			if(playerLifecycle.getPlayerStatus().equals("INITED")) playerLifecycle.setPlayerStatus("normal");
			
			//REMOVE MINIGUN
			if(e.getPlayer().getArmedWeapon().getId() == 38){
				if(!allowMinigun) e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
			}
		});

//PlayerDisconnectEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerDisconnectEvent.class, (e) -> {
			saveWeapons(e.getPlayer());
		});

//PlayerSpawnEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerSpawnEvent.class, (e) -> {
			if(e.getPlayer().getState() != PlayerState.NONE) {
				playerLifecycle.setAnimationIndex(0);
				Shoebill.get().runOnSampThread(() -> {
					giveNormalWeapons(e.getPlayer());
				});
				playerLifecycle.setPlayerStatus("normal");
			}
		});

//PlayerDeathEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerDeathEvent.class, (e) -> {
			
		});
		
//PlayerKeyStateChangeEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerKeyStateChangeEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			if(e.getPlayer().getKeyState().isKeyPressed(PlayerKey.NO)
			&& !e.getPlayer().isInAnyVehicle()){
				if(playerLifecycle.getHoldingKey() == 0){
					playerLifecycle.setHoldingKey(1);
					keyCheck(e.getPlayer());
				}
			}
		});
	}

	private void keyCheck(Player player) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(player.getKeyState().isKeyPressed(PlayerKey.NO)
		&& PlayerState.WASTED != player.getState()
		&& player.getHealth() != 0){
			playerLifecycle.setHoldingKey(playerLifecycle.getHoldingKey()+1);
			
			if(playerLifecycle.getHoldingKey() == 20){ //1sek
				showWeaponShop(player);
			}
		}
		else {
			try {
				if(playerLifecycle.getHoldingKey() != 0){
					if(playerLifecycle.getHoldingKey() < 20
					&& PlayerState.WASTED != player.getState()
					&& player.getHealth() != 0)
						switchWeaponState(player);
						playerLifecycle.setHoldingKey(0);
				} 
			} catch(Exception e){
				if(playerLifecycle != null){
					playerLifecycle.setHoldingKey(0);
					keyCheck(player);
				}
			}
		}
	}

	private void showWeaponShop(Player player) {
		try {
			WeaponShop weaponShop = new WeaponShop();
			weaponShop.shop(player);
		} catch (IOException e) {
			player.sendMessage(Color.RED, "An error occupied!");
			e.printStackTrace();
		}
	}

	private void switchWeaponState(Player player) {
		WeaponData weaponData = getWeaponData(player, player.getArmedWeapon().getId());
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		switch (weaponData.getAmmoState()) {
			case NORMAL:
				if (weaponData.getFireAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.FIRE);
				else if (weaponData.getExplosiveAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.EXPLOSIVE);
				else if (weaponData.getHeavyAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.HEAVY);
				else if (weaponData.getSpecialAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.SPECIAL);
				else if (weaponData.getNormalAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.NORMAL);
				break;
			case FIRE:
				if (weaponData.getExplosiveAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.EXPLOSIVE);
				else if (weaponData.getHeavyAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.HEAVY);
				else if (weaponData.getSpecialAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.SPECIAL);
				else if (weaponData.getNormalAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.NORMAL);
				else if (weaponData.getFireAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.FIRE);
				break;
			case EXPLOSIVE:
				if (weaponData.getHeavyAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.HEAVY);
				else if (weaponData.getSpecialAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.SPECIAL);
				else if (weaponData.getNormalAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.NORMAL);
				else if (weaponData.getFireAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.FIRE);
				else if (weaponData.getExplosiveAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.EXPLOSIVE);
				break;
			case HEAVY:
				if (weaponData.getSpecialAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.SPECIAL);
				else if (weaponData.getNormalAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.NORMAL);
				else if (weaponData.getFireAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.FIRE);
				else if (weaponData.getExplosiveAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.EXPLOSIVE);
				else if (weaponData.getHeavyAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.HEAVY);
				break;
			case SPECIAL:
				if (weaponData.getNormalAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.NORMAL);
				else if (weaponData.getFireAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.FIRE);
				else if (weaponData.getExplosiveAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.EXPLOSIVE);
				else if (weaponData.getHeavyAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.HEAVY);
				else if (weaponData.getSpecialAmmo() > 0) switchAmmoType(player, weaponData, AmmoState.SPECIAL);
				break;
		}
	}

	public void OnPlayerChangeWeapon(Player player, int oldWeapon, int newWeapon) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		WeaponData weaponData = getWeaponData(player, newWeapon);
		if(isGun(newWeapon)){
			if(!playerLifecycle.getPlayerStatus().equals("INIT")){
				if(!player.isInAnyVehicle()){
					animationWeaponsReload(player, newWeapon);
					setWeaponStatusText(player, weaponData.getAmmoState());
					unselectWeapons(player, newWeapon, "weaponId");
					weaponData.setSelected(true);
					weaponData.setAble(true);
				} /*else {
					//TODO: Give Old Weapon !
				}*/
			}
		}
		else {
			if(playerLifecycle.getPlayerStatus().equals("reloaded")
			|| playerLifecycle.getPlayerStatus().equals("reloading")){
				playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), true);
				animationClear(player, playerLifecycle.getPlayerTimer());
			}
			if(playerLifecycle.getWeaponStatusText() != null) playerLifecycle.getWeaponStatusText().hide(player);
		}
	}

	private void OnPlayerUpdateWeapon(Player player, int weaponId) {
		WeaponData weaponData = getWeaponData(player, weaponId);
		if (weaponData.getAmmoState() == null) weaponData.setAmmoState(AmmoState.NORMAL);
		if (weaponData.getAmmoState() == AmmoState.NORMAL) weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
		else if (weaponData.getAmmoState() == AmmoState.FIRE) weaponData.setFireAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
		else if (weaponData.getAmmoState() == AmmoState.EXPLOSIVE) weaponData.setExplosiveAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
		else if (weaponData.getAmmoState() == AmmoState.HEAVY) weaponData.setHeavyAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
		else if (weaponData.getAmmoState() == AmmoState.SPECIAL) weaponData.setSpecialAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
		addWeaponData(player, weaponId, weaponData);
	}

	private void afterWeaponShot(PlayerWeaponShotEvent e, WeaponData weaponData) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
		if(isGun(weaponData.getWeaponId()) || isRechargeable(weaponData.getWeaponId())){
			if (weaponData.getNormalAmmo() > 0
			|| weaponData.getFireAmmo() > 0
			|| weaponData.getExplosiveAmmo() > 0
			|| weaponData.getHeavyAmmo() > 0
			|| weaponData.getSpecialAmmo() > 0) {
				switch (weaponData.getAmmoState()) {
					case NORMAL: {
						boolean ready = false;
						if (weaponData.getNormalAmmo() > 0) {
							ready = true;
							weaponData.setNormalAmmo(weaponData.getNormalAmmo() - 1);
							e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getNormalAmmo());
						}
						if (weaponData.getNormalAmmo() <= 0 && ready) {
							if (weaponData.getFireAmmo() > 0) 
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.FIRE);
							else if (weaponData.getExplosiveAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.EXPLOSIVE);
							else if (weaponData.getHeavyAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.HEAVY);
							else if (weaponData.getSpecialAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.SPECIAL);
						}
						break;
					}
					case FIRE: {
						boolean ready = false;
						if (weaponData.getFireAmmo() > 0) {
							ready = true;
							weaponData.setFireAmmo(weaponData.getFireAmmo() - 1);
							e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getFireAmmo());
						}
						if (weaponData.getFireAmmo() <= 0 && ready) {
							if (weaponData.getExplosiveAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.EXPLOSIVE);
							else if (weaponData.getHeavyAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.HEAVY);
							else if (weaponData.getSpecialAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.SPECIAL);
							else if (weaponData.getNormalAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.NORMAL);
						}
						break;
					}
					case EXPLOSIVE: {
						boolean ready = false;
						if (weaponData.getExplosiveAmmo() > 0) {
							ready = true;
							weaponData.setExplosiveAmmo(weaponData.getExplosiveAmmo() - 1);
							e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getExplosiveAmmo());
						}
						if (weaponData.getExplosiveAmmo() <= 0 && ready) {
							if (weaponData.getHeavyAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.HEAVY);
							else if (weaponData.getSpecialAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.SPECIAL);
							else if (weaponData.getNormalAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.NORMAL);
							else if (weaponData.getFireAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.FIRE);
						}
						break;
					}
					case HEAVY: {
						boolean ready = false;
						if (weaponData.getHeavyAmmo() > 0) {
							ready = true;
							weaponData.setHeavyAmmo(weaponData.getHeavyAmmo() - 1);
							e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getHeavyAmmo());
						}
						if (weaponData.getHeavyAmmo() <= 0 && ready) {
							if (weaponData.getSpecialAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.SPECIAL);
							else if (weaponData.getNormalAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.NORMAL);
							else if (weaponData.getFireAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.FIRE);
							else if (weaponData.getExplosiveAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.EXPLOSIVE);
						}
						break;
					}
					case SPECIAL: {
						boolean ready = false;
						if (weaponData.getSpecialAmmo() > 0) {
							ready = true;
							weaponData.setSpecialAmmo(weaponData.getSpecialAmmo() - 1);
							e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getSpecialAmmo());
						}
						if (weaponData.getSpecialAmmo() <= 0 && ready) {
							if (weaponData.getNormalAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.NORMAL);
							else if (weaponData.getFireAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.FIRE);
							else if (weaponData.getExplosiveAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.EXPLOSIVE);
							else if (weaponData.getHeavyAmmo() > 0)
								switchAmmoType(e.getPlayer(), weaponData, AmmoState.HEAVY);
						}
						break;
					}
				}
				if(weaponData.getNormalAmmo() <= 0
				&& weaponData.getFireAmmo() <= 0
				&& weaponData.getExplosiveAmmo() <= 0
				&& weaponData.getHeavyAmmo() <= 0
				&& weaponData.getSpecialAmmo() <= 0){
					weaponData.setSelected(false);
				}
			}
			if(debug) e.getPlayer().sendMessage("Muni: " + weaponData.getNormalAmmo() + " / " + e.getPlayer().getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getAmmo() + " - " + weaponData.getAmmoState().toString());
		}
	}

	private void switchAmmoType(Player player, WeaponData weaponData, AmmoState ammotyp) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(isGun(player.getArmedWeapon().getId())){
			weaponData.setAmmoState(ammotyp);
			switch (ammotyp) {
				case NORMAL:
					player.setWeaponAmmo(player.getArmedWeapon(), 0);
					player.giveWeapon(player.getArmedWeapon(), weaponData.getNormalAmmo());
					break;
				case FIRE:
					player.setWeaponAmmo(player.getArmedWeapon(), 0);
					player.giveWeapon(player.getArmedWeapon(), weaponData.getFireAmmo());
					break;
				case EXPLOSIVE:
					player.setWeaponAmmo(player.getArmedWeapon(), 0);
					player.giveWeapon(player.getArmedWeapon(), weaponData.getExplosiveAmmo());
					break;
				case HEAVY:
					player.setWeaponAmmo(player.getArmedWeapon(), 0);
					player.giveWeapon(player.getArmedWeapon(), weaponData.getHeavyAmmo());
					break;
				case SPECIAL:
					player.setWeaponAmmo(player.getArmedWeapon(), 0);
					player.giveWeapon(player.getArmedWeapon(), weaponData.getSpecialAmmo());
					break;
			}
			animationWeaponsReload(player, player.getArmedWeapon().getId());
			setWeaponStatusText(player, weaponData.getAmmoState());
		}
	}

	/*
	private void animationWeaponsReload(Player player, int weaponId) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(!playerLifecycle.getPlayerStatus().equals("reloaded")
		&& !playerLifecycle.getPlayerStatus().equals("reloading")){
			playerLifecycle.setPlayerStatus("reloading");
			player.clearAnimations(1);
			if(weaponId == 24 || weaponId == 22) player.applyAnimation("COLT45", "colt45_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1);
			else if(weaponId == 26) player.applyAnimation("COLT45", "sawnoff_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Sawnoff
			else if(weaponId == 23) player.applyAnimation("SILENCED", "Silence_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Silenced
			else if(weaponId == 32) player.applyAnimation("TEC", "TEC_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //TEC-9
			else if(weaponId == 28) player.applyAnimation("UZI", "UZI_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Micro SMG/UZI
			else if(WeaponModel.get(weaponId).getSlot().getSlotId() == 7) player.applyAnimation("BUDDY", "buddy_crouchreload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime*2, 1);
			else player.applyAnimation("BUDDY", "buddy_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1);
	        playerLifecycle.setPlayerStatus("reloaded");
	        playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), true);

	        playerLifecycle.setAnimationIndex(playerLifecycle.getAnimationIndex()+1);
	        final int currentAnimationIndex = playerLifecycle.getAnimationIndex();
	        
	        //TODO: nur für einen Spieler machen ! -> wenn Spieler animationcleared und anderer läuft -> beide bekommen animation gecleared!
	        playerLifecycle.getPlayerTimer().schedule(new TimerTask() {
				@Override
	            public void run() {
					Shoebill.get().runOnSampThread(() -> {
						if(currentAnimationIndex == playerLifecycle.getAnimationIndex()){
							animationClear(player, playerLifecycle.getPlayerTimer());
						}
					});
				}
	        }, WeaponModel.get(weaponId).getSlot().getSlotId()==7?changeWeaponFreezingTime*2:changeWeaponFreezingTime);
		} else {
			playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), false);
			player.clearAnimations(1);
           	playerLifecycle.setPlayerStatus("normal");
			animationWeaponsReload(player, weaponId);
		}
	}
	*/
	private void animationWeaponsReload(Player player, int weaponId) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(!playerLifecycle.getPlayerStatus().equals("reloaded")
		&& !playerLifecycle.getPlayerStatus().equals("reloading")){
			playerLifecycle.setPlayerStatus("reloading");
			player.clearAnimations(1);
			if(weaponId == 24 || weaponId == 22) player.applyAnimation("COLT45", "colt45_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1);
			else if(weaponId == 26) player.applyAnimation("COLT45", "sawnoff_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Sawnoff
			else if(weaponId == 23) player.applyAnimation("SILENCED", "Silence_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Silenced
			else if(weaponId == 32) player.applyAnimation("TEC", "TEC_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //TEC-9
			else if(weaponId == 28) player.applyAnimation("UZI", "UZI_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1); //Micro SMG/UZI
			else if(WeaponModel.get(weaponId).getSlot().getSlotId() == 7) player.applyAnimation("BUDDY", "buddy_crouchreload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime*2, 1);
			else player.applyAnimation("BUDDY", "buddy_reload", 4.1f, 1, 1, 1, changeWeaponFreezingTime, changeWeaponFreezingTime, 1);
	        playerLifecycle.setPlayerStatus("reloaded");
	        playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), true);

	        playerLifecycle.setAnimationIndex(playerLifecycle.getAnimationIndex()+1);
	        final int currentAnimationIndex = playerLifecycle.getAnimationIndex();
	        
	        //TODO: nur für einen Spieler machen ! -> wenn Spieler animationcleared und anderer läuft -> beide bekommen animation gecleared!
	        playerLifecycle.getPlayerTimer().schedule(new TimerTask() {
				@Override
	            public void run() {
					Shoebill.get().runOnSampThread(() -> {
						if (debug) player.sendMessage("currentAnimationIndex: " + currentAnimationIndex + " / " + playerLifecycle.getAnimationIndex());
						if (currentAnimationIndex == playerLifecycle.getAnimationIndex()) {
							animationClear(player, playerLifecycle.getPlayerTimer());
						}
					});
				}
	        }, WeaponModel.get(weaponId).getSlot().getSlotId()==7?changeWeaponFreezingTime*2:changeWeaponFreezingTime);
		} else {
			playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), false);
			player.clearAnimations(1);
           	playerLifecycle.setPlayerStatus("normal");
			animationWeaponsReload(player, weaponId);
		}
	}

	private void animationClear(Player player, Timer timer) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(playerLifecycle.getAnimationReady(timer)){
			Shoebill.get().runOnSampThread(() -> player.clearAnimations(1));
           	playerLifecycle.setPlayerStatus("normal");
           	playerLifecycle.setAnimationIndex(0);
           	playerLifecycle.setAnimationReady(playerLifecycle.getPlayerTimer(), false);
		}
	}

	private void setWeaponStatusText(Player player, AmmoState ammoState) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		String string = ammoState.getDisplayName();
		//mid(525|6)
		float xFloat = 525;
		for (int i = 0; i < (string.length() % 2 == 0 ? string.length() / 2 : (string.length() / 2) + 1); i++) {
			//TODO: für jeden Char bst Breite angeben und mitberechnen
			xFloat -= 7.2f;
		}

		Textdraw weaponStatusText;
		if (playerLifecycle.getWeaponStatusText() != null) {
			weaponStatusText = playerLifecycle.getWeaponStatusText();
			weaponStatusText.hide(player);
			weaponStatusText.setText(string);
		} else {
			weaponStatusText = Textdraw.create(xFloat, 6, string);
		}

		weaponStatusText.setFont(TextDrawFont.get(1));
		weaponStatusText.setLetterSize(0.4f, 2.8000000000000003f);
		weaponStatusText.setColor(Color.WHITE); //0xffffffFF
		weaponStatusText.setProportional(true);
		weaponStatusText.setShadowSize(1);
		weaponStatusText.show(player);

		playerLifecycle.setWeaponStatusText(weaponStatusText);
	}

	void initGlobalTimers(){ //TODO: vllt bei onPlayerUpdateEvent ! vllt mit dem Timer siehe Factions oder so
		globalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
				Shoebill.get().runOnSampThread(() -> {
					for(Player player : Player.getHumans())
	            		keyCheck(player);
            	});
            }
        }, 2000, 50);
	}

	private void initWeapons(Player player) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);	
		if (!WeaponSystem.getInstance().getMysqlConnection().exist(player)) {
			WeaponSystem.getInstance().getMysqlConnection().create(player);
		}
		for(int i=0;i<=46;i++){
			if(i==19||i==20||i==21) continue;
			initWeapon(player, i);
		}
	}
	private void initWeapon(Player player, int i){
		WeaponData weaponData;
		if (!hasWeaponData(player, i)) {
			weaponData = new WeaponData(player.getName(), i);
			weaponData.setAble(WeaponSystem.getInstance().getMysqlConnection().isAble(player, i));
			weaponData.setSelected(WeaponSystem.getInstance().getMysqlConnection().getSelected(player, i));
			if (weaponData.isSelected()) {
				unselectWeapons(player, i, "weaponId");
				weaponData.setSelected(true);
			}
			weaponData.setNormalAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, AmmoState.NORMAL));
			weaponData.setFireAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, AmmoState.FIRE));
			weaponData.setExplosiveAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, AmmoState.EXPLOSIVE));
			weaponData.setHeavyAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, AmmoState.HEAVY));
			weaponData.setSpecialAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, AmmoState.SPECIAL));
			weaponData.setAmmoState(AmmoState.NORMAL);
			
			if (weaponData.getNormalAmmo() <= 0) weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getAmmo());
		} else {
			weaponData = getWeaponData(player, i);
			if (weaponData.getWeaponId() == 0 && WeaponModel.get(i).getSlot().getSlotId() != 0) {
				int weaponId = i;
				weaponData = new WeaponData(player.getName(), weaponId);
				if (player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId())
					weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
				else weaponData.setNormalAmmo(0);
				weaponData.setAmmoState(AmmoState.NORMAL);
			}
		}

		if (weaponData.getNormalAmmo() > 0) {
			player.setWeaponAmmo(WeaponModel.get(i), 0);
			player.giveWeapon(WeaponModel.get(i), weaponData.getNormalAmmo());
			weaponData.setAmmoState(AmmoState.NORMAL);
		} else if (weaponData.getFireAmmo() > 0) {
			player.setWeaponAmmo(WeaponModel.get(i), 0);
			player.giveWeapon(WeaponModel.get(i), weaponData.getFireAmmo());
			weaponData.setAmmoState(AmmoState.FIRE);
		} else if (weaponData.getExplosiveAmmo() > 0) {
			player.setWeaponAmmo(WeaponModel.get(i), 0);
			player.giveWeapon(WeaponModel.get(i), weaponData.getExplosiveAmmo());
			weaponData.setAmmoState(AmmoState.EXPLOSIVE);
		} else if (weaponData.getHeavyAmmo() > 0) {
			player.setWeaponAmmo(WeaponModel.get(i), 0);
			player.giveWeapon(WeaponModel.get(i), weaponData.getHeavyAmmo());
			weaponData.setAmmoState(AmmoState.HEAVY);
		} else if (weaponData.getSpecialAmmo() > 0) {
			player.setWeaponAmmo(WeaponModel.get(i), 0);
			player.giveWeapon(WeaponModel.get(i), weaponData.getSpecialAmmo());
			weaponData.setAmmoState(AmmoState.SPECIAL);
		} else if(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getAmmo() > 0
			   && player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId()){ //TODO: INIT extern weapons from PAWN
			weaponData.setAmmoState(AmmoState.NORMAL);
			weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getAmmo());
		}
		
		if(weaponData.getNormalAmmo() > 0
		|| weaponData.getFireAmmo() > 0
		|| weaponData.getExplosiveAmmo() > 0
		|| weaponData.getHeavyAmmo() > 0
		|| weaponData.getSpecialAmmo() > 0){
			unselectWeapons(player, weaponData.getWeaponId(), "weaponId");
			weaponData.setAble(true);
			weaponData.setSelected(true);
		}

		addWeaponData(player, weaponData.getWeaponId(), weaponData);
	}
	
	private void saveWeapons(Player player){
		for (int i=0; i<=46; i++) {
			if(i==19||i==20||i==21) continue;
			WeaponData weaponData = getWeaponData(player, i);
			WeaponSystem.getInstance().getMysqlConnection().setWeapon(player, i, weaponData.isAble());
			WeaponSystem.getInstance().getMysqlConnection().setSelected(player, i, weaponData.isSelected());
			WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getNormalAmmo(), AmmoState.NORMAL);
			WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getFireAmmo(), AmmoState.FIRE);
			WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getExplosiveAmmo(), AmmoState.EXPLOSIVE);
			WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getHeavyAmmo(), AmmoState.HEAVY);
			WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getSpecialAmmo(), AmmoState.SPECIAL);
		}
	}
	//TODO: WeaponShop Price bug with WeaponId 36 + 37,...?
	
	private void giveNormalWeapons(Player player){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		playerLifecycle.setPlayerStatus("INIT");
		for (int i = 0; i <= 12; i++) {
			//TODO: BUG ! Do it WITHOUT MYSQL, bcus if u give a weapon ingame, the weapon will not be load after dead or so on
			Integer weaponId = WeaponSystem.getInstance().getMysqlConnection().getWeapon(player, i);
			WeaponData weaponData = getWeaponData(player, weaponId == 0 && i != 0 ? player.getWeaponData(i).getModel().getId() : weaponId);
			if (weaponId == 0 && i != 0) {
				if(weaponData.getNormalAmmo() <= 0
				&& weaponData.getExplosiveAmmo() <= 0
				&& weaponData.getFireAmmo() <= 0
				&& weaponData.getHeavyAmmo() <= 0
				&& weaponData.getSpecialAmmo() <= 0
				&& player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId()) {
					weaponData.setNormalAmmo(player.getWeaponData(i).getAmmo());
					weaponData.setAmmoState(AmmoState.NORMAL);
				}
			}
			player.setWeaponAmmo(WeaponModel.get(weaponData.getWeaponId()), 0);

			unselectWeapons(player, i, "slot");
			weaponData.setSelected(true);
			weaponData.setAble(true);

			if (weaponData.getNormalAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponData.getWeaponId()), weaponData.getNormalAmmo());
				weaponData.setAmmoState(AmmoState.NORMAL);
			} else if (weaponData.getFireAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponData.getWeaponId()), weaponData.getFireAmmo());
				weaponData.setAmmoState(AmmoState.FIRE);
			} else if (weaponData.getExplosiveAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponData.getWeaponId()), weaponData.getExplosiveAmmo());
				weaponData.setAmmoState(AmmoState.EXPLOSIVE);
			} else if (weaponData.getHeavyAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponData.getWeaponId()), weaponData.getHeavyAmmo());
				weaponData.setAmmoState(AmmoState.HEAVY);
			} else if (weaponData.getSpecialAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponData.getWeaponId()), weaponData.getSpecialAmmo());
				weaponData.setAmmoState(AmmoState.SPECIAL);
			}
			addWeaponData(player, weaponData.getWeaponId(), weaponData);
			if (isGun(weaponData.getWeaponId())) setWeaponStatusText(player, weaponData.getAmmoState());
		}

		for(int i=0;i<46;i++){
			if(i==19||i==20||i==21) continue;
			WeaponData weaponData = getWeaponData(player, i);
			if(weaponData.getNormalAmmo() <= 0 && weaponData.getAmmoState() == AmmoState.NORMAL){
				if (player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId())
					weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getAmmo());
			}
			if(weaponData.getNormalAmmo() > 0
			|| weaponData.getFireAmmo() > 0
			|| weaponData.getExplosiveAmmo() > 0
			|| weaponData.getHeavyAmmo() > 0
			|| weaponData.getSpecialAmmo() > 0){
				weaponData.setAble(true);
			}
			if(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getModel().getId() == i){
				unselectWeapons(player, i, "weaponId");
				weaponData.setSelected(true);
				weaponData.setAble(true);
			}
			addWeaponData(player, i, weaponData);
		}
		playerLifecycle.setPlayerStatus("INITED");
	}

	public void givePlayerExternWeapon(Player player, int weaponId){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		final WeaponData weaponData = getWeaponData(player, weaponId);
		if(weaponData.getWeaponId() == 0 && WeaponModel.get(weaponId).getSlot().getSlotId() != 0){
			if(weaponData.getNormalAmmo() <= 0
			&& weaponData.getExplosiveAmmo() <= 0
			&& weaponData.getFireAmmo() <= 0
			&& weaponData.getHeavyAmmo() <= 0
			&& weaponData.getSpecialAmmo() <= 0
			&& player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId()){
				weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
				weaponData.setAmmoState(AmmoState.NORMAL);
			}
		}

		player.setWeaponAmmo(WeaponModel.get(weaponId), 0);
		unselectWeapons(player, weaponId, "weaponId");
		weaponData.setSelected(true);
		weaponData.setAble(true);
		//TODO: Why ?
		if (weaponData.getAmmoState() == null
		|| weaponData.getAmmoState() == AmmoState.NORMAL && weaponData.getNormalAmmo() <= 0
		|| weaponData.getAmmoState() == AmmoState.FIRE && weaponData.getFireAmmo() <= 0
		|| weaponData.getAmmoState() == AmmoState.EXPLOSIVE && weaponData.getExplosiveAmmo() <= 0
		|| weaponData.getAmmoState() == AmmoState.HEAVY && weaponData.getHeavyAmmo() <= 0
		|| weaponData.getAmmoState() == AmmoState.SPECIAL && weaponData.getSpecialAmmo() <= 0) {
			if (weaponData.getNormalAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getNormalAmmo());
				weaponData.setAmmoState(AmmoState.NORMAL);
			} else if (weaponData.getFireAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getFireAmmo());
				weaponData.setAmmoState(AmmoState.FIRE);
			} else if (weaponData.getExplosiveAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getExplosiveAmmo());
				weaponData.setAmmoState(AmmoState.EXPLOSIVE);
			} else if (weaponData.getHeavyAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getHeavyAmmo());
				weaponData.setAmmoState(AmmoState.HEAVY);
			} else if (weaponData.getSpecialAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getSpecialAmmo());
				weaponData.setAmmoState(AmmoState.SPECIAL);
			}
		} else {
			switch (weaponData.getAmmoState()) {
				case NORMAL:
					player.giveWeapon(WeaponModel.get(weaponId), weaponData.getNormalAmmo());
					break;
				case FIRE:
					player.giveWeapon(WeaponModel.get(weaponId), weaponData.getFireAmmo());
					break;
				case EXPLOSIVE:
					player.giveWeapon(WeaponModel.get(weaponId), weaponData.getExplosiveAmmo());
					break;
				case HEAVY:
					player.giveWeapon(WeaponModel.get(weaponId), weaponData.getHeavyAmmo());
					break;
				case SPECIAL:
					player.giveWeapon(WeaponModel.get(weaponId), weaponData.getSpecialAmmo());
					break;
			}
		}
		addWeaponData(player, weaponId, weaponData);
		if (isGun(weaponId)) setWeaponStatusText(player, weaponData.getAmmoState());
	}
	
	private void createWarnExplosion(Player player) {
		float health = player.getHealth();
		player.createExplosion(player.getLocation(), 12, 1);
		player.setHealth(health - 5);
	}

    public void addWeaponData(Player player, Integer key, WeaponData weaponData){
    	HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
		if (innerMap == null) innerMap = new HashMap<>();
		if (isRechargeable(key)) {
			weaponData.setAmmoState(AmmoState.NORMAL);
			weaponData.setNormalAmmo(weaponData.getNormalAmmo()+weaponData.getFireAmmo()+weaponData.getExplosiveAmmo()+weaponData.getHeavyAmmo()+weaponData.getSpecialAmmo());
			weaponData.setFireAmmo(0);
			weaponData.setExplosiveAmmo(0);
			weaponData.setHeavyAmmo(0);
			weaponData.setSpecialAmmo(0);
		} else if (!isGun(key)) {
			weaponData.setAmmoState(AmmoState.NORMAL);
			if (weaponData.getNormalAmmo() > 0) weaponData.setNormalAmmo(1);
			else weaponData.setNormalAmmo(0);
			weaponData.setFireAmmo(0);
			weaponData.setExplosiveAmmo(0);
			weaponData.setHeavyAmmo(0);
			weaponData.setSpecialAmmo(0);
		} else if (isGun(key)) {
			if(weaponData.getAmmoState() == AmmoState.NORMAL && weaponData.getNormalAmmo() <= 0
			&& player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId())
				weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getAmmo());
		}
		
		innerMap.put(key, weaponData);
		weaponDataMap.put(player, innerMap);
    }

    public WeaponData getWeaponData(Player player, Integer key){
    	if(key == 19 || key == 20 || key == 21) return null;
        HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
        if(innerMap == null || !innerMap.containsKey(key)){
        //to import extern imported Weapons
			WeaponData weaponData = new WeaponData(player.getName(), key);

			//Initialize new Weapon
			weaponData.setAmmoState(AmmoState.NORMAL);
			try {
				if (player.getWeaponData(WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId()).getModel().getId() == weaponData.getWeaponId())
					weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(key).getSlot().getSlotId()).getAmmo());
				else weaponData.setNormalAmmo(0);
			} catch (Exception e) {
				weaponData.setNormalAmmo(0);
			}
			weaponData.setFireAmmo(0);
			weaponData.setExplosiveAmmo(0);
			weaponData.setHeavyAmmo(0);
			weaponData.setSpecialAmmo(0);
			
			weaponData.setAble(false);
			weaponData.setSelected(false);

			//Add the weapon to the HashMap
			addWeaponData(player, key, weaponData);
        	return weaponData;
        }
        return innerMap.get(key);
    }

	public boolean hasWeaponData(Player player, Integer key){
    	if(key == 19 || key == 20 || key == 21) return false; //TODO
        HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
		if (innerMap == null || !innerMap.containsKey(key)) return false;
		else return true;
	}

    void unselectWeapons(Player player, int value, String typ) {
		for(int weaponId : WeaponSystem.getInstance().getMysqlConnection().getSlotWeapons(typ.equals("slot")?value:WeaponModel.get(value).getSlot().getSlotId())){
		//	if(hasWeaponData(player, weaponId)){ //TODO: FIX
				WeaponData weaponData = getWeaponData(player, weaponId);
				weaponData.setSelected(false);
				addWeaponData(player, weaponData.getWeaponId(), weaponData);
		//	}
		}
	}

	private void togglePlayerBurning(Player player, boolean burning) {
		if(!isPlayerInWater(player)){
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
    		if(burning) {
				PlayerObject.create(player, 18688, new Location(player.getLocation().x, player.getLocation().y, player.getLocation().z), 0, 0, 0);
				playerLifecycle.setHealth(player.getHealth());
    			playerLifecycle.setPlayerStatus("ignited");

                fireTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
						Shoebill.get().runOnSampThread(() -> burningTimer(player));
					}
                }, 0, 1000);
                fireTimer2.schedule(new TimerTask() {
                    @Override
                    public void run() {
						Shoebill.get().runOnSampThread(() -> {
							togglePlayerBurning(player, false);
							fireTimer.cancel();
						});
					}
                }, 7000);
	        } else {
				PlayerObject.get(player, 18688).destroy();
				playerLifecycle.setPlayerStatus("normal");
			}
    	}
    }

    private boolean isPlayerInWater(Player player){
    	float z = player.getLocation().z;
		return (1544 >= player.getAnimationIndex() && player.getAnimationIndex() >= 1538 || player.getAnimationIndex() == 1062 || player.getAnimationIndex() == 1250) && (z <= 0 || (z <= 41.0 && isPlayerInArea(player, -1387, -473, 2025, 2824))) ||
				(1544 >= player.getAnimationIndex() && player.getAnimationIndex() >= 1538 || player.getAnimationIndex() == 1062 || player.getAnimationIndex() == 1250) && (z <= 2 || (z <= 39.0 && isPlayerInArea(player, -1387, -473, 2025, 2824)));
	}

	private boolean isPlayerInArea(Player player, float minX, float maxX, float minY, float maxY) {
		float x = player.getLocation().x;
    	float y = player.getLocation().y;
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}

	boolean isGun(int weaponId) {
		if(weaponId==19||weaponId==20||weaponId==21) return false;
		if(WeaponModel.get(weaponId).getSlot().getSlotId() != 0
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 1
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 8
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 9
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 10
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 11
		&& WeaponModel.get(weaponId).getSlot().getSlotId() != 12)
			return true;
		return false;
	}
	
	boolean isRechargeable(int weaponId) {
		if(weaponId==19||weaponId==20||weaponId==21) return false;
		else if(WeaponModel.get(weaponId).getSlot().getSlotId() == 9
			 || WeaponModel.get(weaponId).getSlot().getSlotId() == 8) return true;
		return false;
	}

	private void burningTimer(Player player){
		player.setHealth(player.getHealth() - 2.0f);
	}
    
	public void uninitialize()
	{
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public boolean isDestroyed() {
		return true;
	}
}
