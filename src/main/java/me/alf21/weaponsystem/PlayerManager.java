package me.alf21.weaponsystem;

import net.gtaun.shoebill.constant.PlayerKey;
import net.gtaun.shoebill.constant.PlayerState;
import net.gtaun.shoebill.constant.TextDrawFont;
import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.constant.WeaponSlot;
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
 * Created by Alf21 on 28.04.2015 in project weapon_system.
 * Copyright (c) 2015 Alf21. All rights reserved.
 **/


/*
 * Bugs:
 *         
 * - Wenn man durch eine Explosion getoetet wurde, welche durch einen Schuss eines Spielers ausgeloest wurde:
 *   Lsg.: Bekommen der Position der Explosion
 *         Bekommen des Spielers, der dorthin bzw. in die Naehe geschossen hat
 *         Ihn als Killer setzen
 *         
 */
//TODO: Bug, wenn man Waffe wechselt und schnell zu Slot 1 wechselt -> Animation wird nicht abgebrochen
//TODO: Weaponstatus unter WeaponData, nicht Lifecycle / PlayerData, denn wenn man Waffe wechselt -> Andere Waffe hat auch Brandmuni,
//		Man wechselt die Munitionsart, Explosiv, dann zurück zur alten, dann hat diese Waffe auch den Waffenstatus / Munitionsart Explosiv ausgewählt

//TODO: REMOVE muni_LOADED !
//TODO: Animation, wenn man Weaponshop betritt, zB hinhocken und in ner Tasche rumkramen usw..

public class PlayerManager implements Destroyable {
	public static final boolean allowMinigun = false; //false, if u want better performance bcus of spamming and calculation for each ammo on shot
	public static final int brandObjectsUpdateTime = 200; //Time when the next brandObject can create FOR EACH PLAYER !
	//Config
	private static final boolean allowMinigunAmmoTypes = false;
	private static final int changeWeaponFreezingTime = 1000; //freezingtime in miliseconds | 0, if no freeze
	private static final int maxBrandObjects = 50; // FOR EACH PLAYER !
	public PlayerData playerLifecycle;
	private PlayerData externPlayerLifecycle;
	private Map<Player, HashMap<Integer, WeaponData>> weaponDataMap;
	private Timer globalTimer = new Timer();

	public PlayerManager()
	{
        this.weaponDataMap = new HashMap<Player, HashMap<Integer, WeaponData>>();
        
//PlayerConnectEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerConnectEvent.class, (e) -> {
			WeaponSystem.getInstance().getShoebill().runOnSampThread(() -> {
				playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
				playerLifecycle.setPlayerStatus("connected");
				playerLifecycle.setHoldingKey(0);
				
				INIT_Weapons(e.getPlayer());
			});
		});
		
//PlayerWeaponShotEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerWeaponShotEvent.class, (e) -> {
			WeaponSystem.getInstance().getShoebill().runOnSampThread(() -> {
				playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
				if (e.getHitPlayer() != null)
					externPlayerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getHitPlayer(), PlayerData.class);
				WeaponData weaponData = getWeaponData(e.getPlayer(), e.getPlayer().getArmedWeapon().getId());
				if (weaponData.getExplosiveAmmo() > 0 && weaponData.getWeaponState().equals("explosiv")) {
					for(Player victim : Player.getHumans()){
						victim.setHealth(100);
					}
					WeaponSystem.getInstance().getShoebill().getSampObjectManager().getWorld().createExplosion(new Location(e.getPosition().x, e.getPosition().y, e.getPosition().z), 12, 1);
					for(Player victim : Player.getHumans()){
						PlayerData victimLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(victim, PlayerData.class);
						//TODO: y and z coordinates ! 
						if(victim.getLocation().x-e.getPosition().x<=1&&victim.getLocation().x-e.getPosition().x>=-1){
							victim.setHealth(victimLifecycle.getHealth()-10);
						}
						else if(victim.getLocation().x-e.getPosition().x<=2&&victim.getLocation().x-e.getPosition().x>=-2){
							victim.setHealth(victimLifecycle.getHealth()-5);
						}
						else if(victim.getLocation().x-e.getPosition().x<=3&&victim.getLocation().x-e.getPosition().x>=-3){
							victim.setHealth(victimLifecycle.getHealth()-2);
						}
						else/* if(victims.getLocation().x-e.getPosition().x<4&&victims.getLocation().x-e.getPosition().x>-4)*/{ 
							victim.setHealth(victimLifecycle.getHealth());
						}
					}
					if(e.getHitObject() != null){
						WeaponSystem.getInstance().getShoebill().getSampObjectManager().getWorld().createExplosion(e.getHitObject().getLocation(), 12, 1);
					}
				}
				if (weaponData.getFireAmmo() > 0 && weaponData.getWeaponState().equals("brand")) {
				//TODO: Entscheiden, ob man die Flammen nur für den Spieler oder auch für andere Spieler anzeigt.
					if(e.getHitPlayer() != null) externPlayerLifecycle.setPlayerStatus("igniting");
					else {
						if(playerLifecycle.getBrandObjects()+1 <= maxBrandObjects){ //max. 50 Flammen erstellen in 2sek!
							if(e.getPlayer().getArmedWeapon().getId() == 38 && playerLifecycle.getCreateBrand() && allowMinigunAmmoTypes
							|| playerLifecycle.getCreateBrand()){
								playerLifecycle.setCreateBrand(false);
								playerLifecycle.setBrandObjects(playerLifecycle.getBrandObjects()+1);
								SampObject sampObject = WeaponSystem.getInstance().getShoebill().getSampObjectManager().createObject(18688, new Location(e.getPosition().x, e.getPosition().y, e.getPosition().z + 0.25f), e.getPosition(), 0);
								Timer timer = new Timer();
				                timer.schedule(new TimerTask() {
				                    @Override
				                    public void run() {
						    			sampObject.destroy();
						    			playerLifecycle.setBrandObjects(playerLifecycle.getBrandObjects()-1);
				                    }
				                }, 2000);
							}
						}
					}
				}
				
				//TODO: Bug, wenn man muni alle hat, WeaponState sich ändert, aber StandartMuni nur 1 ist und durch Schuss 0 -> Danach keine Muni mehr in nachfolgenden States, nur davor. Deshalb klasse erstellen : reloadWeapon(Player player, WeaponData weaponData, Integer weaponId, String typ) 
				AfterWeaponShot(e, weaponData);
			});
		});
		
//PlayerGiveDamageEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerGiveDamageEvent.class, (e) -> {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
			WeaponData weaponData = getWeaponData(e.getPlayer(), e.getPlayer().getArmedWeapon().getId());
			float damage = e.getAmount();
			//TODO: NO-DM ZONES 
			if(!playerLifecycle.isNoDM()){
				if(e.getPlayer() != e.getVictim()){
					/*//Event Weapons
					if(e.getWeapon().getId() == 25) { damage = e.getAmount()-70; }
					if(e.getWeapon().getId() == 23) { damage = 100; }
					if(e.getWeapon().getId() == 22) { damage = -5; }
					if(e.getWeapon().getId() == 33) { CREATE_Explosion(e.getPlayer(), e.getVictim(), 5); }
					if(e.getWeapon().getId() == 17) { damage = 2; }
					*/
					if (weaponData.getWeaponState().equals("normal")) {
						damage = (e.getAmount()/100)*50; //50% weniger Schaden / die Haelfte -> nur 50% Schaden
					} else if (weaponData.getWeaponState().equals("brand")) {
						damage = 1;
					} else if (weaponData.getWeaponState().equals("explosiv")) {
						damage = (e.getAmount()/100)*10; //90% weniger Schaden -> 10% Schaden
					} else if (weaponData.getWeaponState().equals("panzerbrechend")) {
						damage = (e.getAmount()/100)*65; //35% weniger Schaden -> 65% Schaden
					} else if (weaponData.getWeaponState().equals("speziell")) {
						damage = (e.getAmount()/100)*120; //20% mehr Schaden -> 120% Schaden
					}
				}
				//Satchel as Medipack
			}
			else {
				e.getVictim().setHealth(e.getVictim().getHealth());
				CREATE_WarnExplosion(e.getPlayer());
			}
		//Give damage (HP && Armour)
			if (e.getPlayer().getArmour() > 0 && e.getPlayer().getArmour() >= damage && !weaponData.getWeaponState().equals("panzerbrechend"))
				e.getVictim().setArmour(e.getVictim().getArmour() - damage);
			else if (e.getPlayer().getArmour() > 0 && e.getPlayer().getArmour() < damage && !weaponData.getWeaponState().equals("panzerbrechend")) {
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
				TogglePlayerBurning(e.getPlayer(), true);
			}
			
			if(e.getPlayer().getArmedWeapon() != null){
				int currentWeapon = e.getPlayer().getArmedWeapon().getId();
				if(currentWeapon != playerLifecycle.getCurrentWeapon()){
					OnPlayerChangeWeapon(e.getPlayer(), playerLifecycle.getCurrentWeapon(), currentWeapon);
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
			SAVE_Weapons(e.getPlayer());
		});

//PlayerSpawnEvent
		WeaponSystem.getInstance().getEventManagerInstance().registerHandler(PlayerSpawnEvent.class, (e) -> {
			WeaponSystem.getInstance().getShoebill().runOnSampThread(() -> {
				if(e.getPlayer().getState() != PlayerState.NONE) {
					GIVE_NormalWeapons(e.getPlayer());
				//	playerLifecycle.setPlayerStatus("spawned");
					playerLifecycle.setPlayerStatus("normal");
					playerLifecycle.setMoney(100000);
				}
			});
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
					KeyCheck(e.getPlayer());
				}
			}
		});
	}
	
	private void KeyCheck(Player player) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(player.getKeyState().isKeyPressed(PlayerKey.NO)){
			playerLifecycle.setHoldingKey(playerLifecycle.getHoldingKey()+1);
			
			if(playerLifecycle.getHoldingKey() == 20){ //1sek
				KEY_NO_Holded(player);
			}
		}
		else {
			try {
				if(playerLifecycle.getHoldingKey() != 0){
					player.getState();
					if(playerLifecycle.getHoldingKey() < 20
					&& PlayerState.WASTED != player.getState()
					&& player.getHealth() != 0)
						KEY_NO_Clicked(player);
					playerLifecycle.setHoldingKey(0);
				} 
			} catch(Exception e){
				if(playerLifecycle != null){
					playerLifecycle.setHoldingKey(0);
					KeyCheck(player);
				}
			}
		}
	}

	private void KEY_NO_Holded(Player player) {
		WeaponShop weaponShop = new WeaponShop();
		try {
			weaponShop.Shop(player);
		} catch (IOException e) {
			System.out.println(e);
			player.sendMessage(Color.RED, "An error occupied!");
			e.printStackTrace();
		}
	}

	private void KEY_NO_Clicked(Player player) {
		WeaponData weaponData = getWeaponData(player, player.getArmedWeapon().getId());
		if (weaponData.getWeaponState().equals("normal")) {
			if (weaponData.getFireAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "brand");
			} else if (weaponData.getExplosiveAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "explosiv");
			} else if (weaponData.getHeavyAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "panzerbrechend");
			} else if (weaponData.getSpecialAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "speziell");
			} else if (weaponData.getNormalAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "normal");
			}
		} else if (weaponData.getWeaponState().equals("brand")) {
			if (weaponData.getExplosiveAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "explosiv");
			} else if (weaponData.getHeavyAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "panzerbrechend");
			} else if (weaponData.getSpecialAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "speziell");
			} else if (weaponData.getNormalAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "normal");
			} else if (weaponData.getFireAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "brand");
			}
		} else if (weaponData.getWeaponState().equals("explosiv")) {
			if (weaponData.getHeavyAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "panzerbrechend");
			} else if (weaponData.getSpecialAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "speziell");
			} else if (weaponData.getNormalAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "normal");
			} else if (weaponData.getFireAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "brand");
			} else if (weaponData.getExplosiveAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "explosiv");
			}
		} else if (weaponData.getWeaponState().equals("panzerbrechend")) {
			if (weaponData.getSpecialAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "speziell");
			} else if (weaponData.getNormalAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "normal");
			} else if (weaponData.getFireAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "brand");
			} else if (weaponData.getExplosiveAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "explosiv");
			} else if (weaponData.getHeavyAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "panzerbrechend");
			}
		} else if (weaponData.getWeaponState().equals("speziell")) {
			if (weaponData.getNormalAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "normal");
			} else if (weaponData.getFireAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "brand");
			} else if (weaponData.getExplosiveAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "explosiv");
			} else if (weaponData.getHeavyAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "panzerbrechend");
			} else if (weaponData.getSpecialAmmo() > 0) {
				SWITCH_Ammotyp(player, weaponData, "speziell");
			}
		}
	}

	public void OnPlayerChangeWeapon(Player player, int oldWeapon, int newWeapon){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		WeaponData weaponData = getWeaponData(player, newWeapon);
		if(isGun(newWeapon)){
			if(!playerLifecycle.getPlayerStatus().equals("INIT")){
				if(!player.isInAnyVehicle()){
					ANIMATION_Weapons_Reload(player, newWeapon);
					SET_WeaponStatusText(player, weaponData.getWeaponState());
					UNSELECT_Weapons(player, newWeapon, "weaponId");
					weaponData.setSelected(true);
				} else {
					//TODO: Give Old Weapon !
				}
			}
		}
		else {
			if(playerLifecycle.getPlayerStatus().equals("reloaded")
			|| playerLifecycle.getPlayerStatus().equals("reloading")){
				playerLifecycle.setAnimationReady(playerLifecycle.getOldTimer(), true);
				ANIMATION_Clear(player, playerLifecycle.getOldTimer(), playerLifecycle.getPlayerStatus());
				playerLifecycle.setAnimationReady(playerLifecycle.getOldTimer(), false);
			}
			if(playerLifecycle.getWeaponStatusText() != null) playerLifecycle.getWeaponStatusText().hide(player);
		}
	}

	private void AfterWeaponShot(PlayerWeaponShotEvent e, WeaponData weaponData) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(e.getPlayer(), PlayerData.class);
		if(isGun(weaponData.getWeaponId())){
			if (weaponData.getNormalAmmo() > 0
			|| weaponData.getFireAmmo() > 0
			|| weaponData.getExplosiveAmmo() > 0
			|| weaponData.getHeavyAmmo() > 0
			|| weaponData.getSpecialAmmo() > 0) {
				if (weaponData.getWeaponState().equals("normal")) {
					boolean ready = false;
					if (weaponData.getNormalAmmo() > 0) {
						ready = true;
						weaponData.setNormalAmmo(weaponData.getNormalAmmo() - 1);
					//	e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
						//	e.getPlayer().giveWeapon(e.getPlayer().getArmedWeapon(), weaponData.getNormalAmmo());
						e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getNormalAmmo());
					}
					if (weaponData.getNormalAmmo() <= 0 && ready) {
						if (weaponData.getFireAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "brand");
						else if (weaponData.getExplosiveAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "explosiv");
						else if (weaponData.getHeavyAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "panzerbrechend");
						else if (weaponData.getSpecialAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "speziell");
					}
				} else if (weaponData.getWeaponState().equals("brand")) {
					boolean ready = false;
					if (weaponData.getFireAmmo() > 0) {
						ready = true;
						weaponData.setFireAmmo(weaponData.getFireAmmo() - 1);
					//	e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
						//	e.getPlayer().giveWeapon(e.getPlayer().getArmedWeapon(), weaponData.getFireAmmo());
						e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getFireAmmo());
					}
					if (weaponData.getFireAmmo() <= 0 && ready) {
						if (weaponData.getExplosiveAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "explosiv");
						else if (weaponData.getHeavyAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "panzerbrechend");
						else if (weaponData.getSpecialAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "speziell");
						else if (weaponData.getNormalAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "normal");
					}
				} else if (weaponData.getWeaponState().equals("explosiv")) {
					boolean ready = false;
					if (weaponData.getExplosiveAmmo() > 0) {
						ready = true;
						weaponData.setExplosiveAmmo(weaponData.getExplosiveAmmo() - 1);
					//	e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
						//	e.getPlayer().giveWeapon(e.getPlayer().getArmedWeapon(), weaponData.getExplosiveAmmo());
						e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getExplosiveAmmo());
					}
					if (weaponData.getExplosiveAmmo() <= 0 && ready) {
						if (weaponData.getHeavyAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "panzerbrechend");
						else if (weaponData.getSpecialAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "speziell");
						else if (weaponData.getNormalAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "normal");
						else if (weaponData.getFireAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "brand");
					}
				} else if (weaponData.getWeaponState().equals("panzerbrechend")) {
					boolean ready = false;
					if (weaponData.getHeavyAmmo() > 0) {
						ready = true;
						weaponData.setHeavyAmmo(weaponData.getHeavyAmmo() - 1);
					//	e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
						//	e.getPlayer().giveWeapon(e.getPlayer().getArmedWeapon(), weaponData.getHeavyAmmo());
						e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getHeavyAmmo());
					}
					if (weaponData.getHeavyAmmo() <= 0 && ready) {
						if (weaponData.getSpecialAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "speziell");
						else if (weaponData.getNormalAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "normal");
						else if (weaponData.getFireAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "brand");
						else if (weaponData.getExplosiveAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "explosiv");
					}
				} else if (weaponData.getWeaponState().equals("speziell")) {
					boolean ready = false;
					if (weaponData.getSpecialAmmo() > 0) {
						ready = true;
						weaponData.setSpecialAmmo(weaponData.getSpecialAmmo() - 1);
					//	e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), 0);
						//	e.getPlayer().giveWeapon(e.getPlayer().getArmedWeapon(), weaponData.getSpecialAmmo());
						e.getPlayer().setWeaponAmmo(e.getPlayer().getArmedWeapon(), weaponData.getSpecialAmmo());
					}
					if (weaponData.getSpecialAmmo() <= 0 && ready) {
						if (weaponData.getNormalAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "normal");
						else if (weaponData.getFireAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "brand");
						else if (weaponData.getExplosiveAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "explosiv");
						else if (weaponData.getHeavyAmmo() > 0) SWITCH_Ammotyp(e.getPlayer(), weaponData, "panzerbrechend");
					}
				}
			}
		}
	}
	
	private void SWITCH_Ammotyp(Player player, WeaponData weaponData, String ammotyp){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		//TODO: ? Statt getNormalAmmo,... alles über die Hashmap regeln -> bessere Performance, einfacher / kürzer
		//		oder den Hashmap getMUNI_Loaded löschen
		//TODO: ? muni_LOADED to get shotable ammo without reload -> for later system -> only reloadAnimation when reloading...
		//TODO: or delete muni_LOADED !
		if(isGun(player.getArmedWeapon().getId())){
			/*
			HashMap<String, Integer> muni_LOADED = new HashMap<String, Integer>();
			if(player.getArmedWeapon() != null){
				if(weaponData.getMUNI_Loaded() != null) muni_LOADED = weaponData.getMUNI_Loaded();
				muni_LOADED.put(weaponData.getWeaponState(), player.getArmedWeaponAmmo());
				if(player.getArmedWeaponAmmo() > 0) weaponData.setMUNI_Loaded(muni_LOADED);
			}
			*/
			
			if(ammotyp.equals("normal")){
				weaponData.setWeaponState("normal");
				player.setWeaponAmmo(player.getArmedWeapon(), 0);
				/*if(muni_LOADED.containsKey("normal")) player.giveWeapon(player.getArmedWeapon(), weaponData.getMUNI_Loaded().get("normal"));
				else */
				player.giveWeapon(player.getArmedWeapon(), weaponData.getNormalAmmo());
			}
			else if(ammotyp.equals("brand")){
				weaponData.setWeaponState("brand");
				player.setWeaponAmmo(player.getArmedWeapon(), 0);
				/*if(muni_LOADED.containsKey("brand")) player.giveWeapon(player.getArmedWeapon(), weaponData.getMUNI_Loaded().get("brand"));
				else */
				player.giveWeapon(player.getArmedWeapon(), weaponData.getFireAmmo());
			}
			else if(ammotyp.equals("explosiv")){
				weaponData.setWeaponState("explosiv");
				player.setWeaponAmmo(player.getArmedWeapon(), 0);
				/*if(muni_LOADED.containsKey("explosiv")) player.giveWeapon(player.getArmedWeapon(), weaponData.getMUNI_Loaded().get("explosiv"));
				else */
				player.giveWeapon(player.getArmedWeapon(), weaponData.getExplosiveAmmo());
			}
			else if(ammotyp.equals("panzerbrechend")){
				weaponData.setWeaponState("panzerbrechend");
				player.setWeaponAmmo(player.getArmedWeapon(), 0);
				/*if(muni_LOADED.containsKey("panzerbrechend")) player.giveWeapon(player.getArmedWeapon(), weaponData.getMUNI_Loaded().get("panzerbrechend"));
				else */
				player.giveWeapon(player.getArmedWeapon(), weaponData.getHeavyAmmo());
			}
			else if(ammotyp.equals("speziell")){
				weaponData.setWeaponState("speziell");
				player.setWeaponAmmo(player.getArmedWeapon(), 0);
				/*if(muni_LOADED.containsKey("speziell")) player.giveWeapon(player.getArmedWeapon(), weaponData.getMUNI_Loaded().get("speziell"));
				else */
				player.giveWeapon(player.getArmedWeapon(), weaponData.getSpecialAmmo());
			}
			ANIMATION_Weapons_Reload(player, player.getArmedWeapon().getId());
			SET_WeaponStatusText(player, weaponData.getWeaponState());
		}
	}

	private void ANIMATION_Weapons_Reload(Player player, int weaponId) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		Timer timer = new Timer();
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
	        playerLifecycle.setOldTimer(timer);
	        playerLifecycle.setAnimationReady(timer, true);

			timer.schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	ANIMATION_Clear(player, timer, playerLifecycle.getPlayerStatus());
	            }
	        }, WeaponModel.get(weaponId).getSlot().getSlotId()==7?changeWeaponFreezingTime*2:changeWeaponFreezingTime);
		}
		else {
			playerLifecycle.setAnimationReady(playerLifecycle.getOldTimer(), false);
			player.clearAnimations(1);
			playerLifecycle.setPlayerStatus("normal");
			ANIMATION_Weapons_Reload(player, weaponId);
		}
	}

	private void ANIMATION_Clear(Player player, Timer timer, String playerStatus) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		if(playerLifecycle.getAnimationReady(timer)){
        	player.clearAnimations(1);
        //	playerLifecycle.setPlayerStatus(playerStatus);
           	playerLifecycle.setPlayerStatus("normal");
    	}
	}
	
	private void SET_WeaponStatusText(Player player, String string){
		if(string != null && !string.equals("")){
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
			String text = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	
			//mid(525|6)
			float xFloat = 525;
			for (int i = 0; i < (string.length()%2==0?string.length()/2:(string.length()/2)+1); i++){
				//TODO: für jeden Char bst Breite angeben und mitberechnen
			    xFloat -= 7.0f;
			}
			
			Textdraw weaponStatusText;
			if(playerLifecycle.getWeaponStatusText() != null){
				weaponStatusText = playerLifecycle.getWeaponStatusText();
				weaponStatusText.hide(player);
				weaponStatusText.setText(text);
			}
			else {
				weaponStatusText = Textdraw.create(xFloat, 6, text);
			}
			
			weaponStatusText.setFont(TextDrawFont.get(1));
			weaponStatusText.setLetterSize(0.4f, 2.8000000000000003f);
			weaponStatusText.setColor(Color.WHITE); //0xffffffFF
			weaponStatusText.setProportional(true);
			weaponStatusText.setShadowSize(1);
			weaponStatusText.show(player);
			
			playerLifecycle.setWeaponStatusText(weaponStatusText);
		}
	}

	void INIT_GLOBAL_Timers(){
		globalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
            	for(Player player : Player.getHumans())
            		KeyCheck(player);
            }
        }, 2000, 50);
	}
	
	private void INIT_Weapons(Player player){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		
		if (!WeaponSystem.getInstance().getMysqlConnection().exist(player)) {
			WeaponSystem.getInstance().getMysqlConnection().create(player);
		}
		int cooldown = 50;
		player.toggleSpectating(true);
		player.sendMessage("Loading...");
		playerLifecycle.setInitializingWeapon(0);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
            @Override
            public void run() {
            	if(playerLifecycle.getInitializingWeapon() <= 46){
	    			INIT_Weapon(player, playerLifecycle.getInitializingWeapon());
	    			playerLifecycle.setInitializingWeapon(playerLifecycle.getInitializingWeapon()+1);
            	} else {
            		timer.cancel();
            		player.sendMessage("Ready!");
            	}
            }
        }, 1000, cooldown);
		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
            @Override
            public void run() {
            	player.toggleSpectating(false);
            }
        }, cooldown*47);
	}
	private void INIT_Weapon(Player player, int i){
		WeaponData weaponData;
		if(!hasWeaponData(player, i)){
			weaponData = new WeaponData(player.getName(), i);
			weaponData.setAble(WeaponSystem.getInstance().getMysqlConnection().isAble(player, i));
			weaponData.setSelected(WeaponSystem.getInstance().getMysqlConnection().getSelected(player, i));
			weaponData.setNormalAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, "normal"));
			weaponData.setFireAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, "brand"));
			weaponData.setExplosiveAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, "explosiv"));
			weaponData.setHeavyAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, "panzerbrechend"));
			weaponData.setSpecialAmmo(WeaponSystem.getInstance().getMysqlConnection().getMUNI(player, i, "speziell"));
		} else {
			weaponData = getWeaponData(player, i);
			if(weaponData.getWeaponId() == 0 && WeaponModel.get(i).getSlot().getSlotId() != 0){
				int weaponId = player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()).getModel().getId();
				weaponData = new WeaponData(player.getName(), weaponId);
				weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
				weaponData.setWeaponState("normal");
			}
		}
		
		if (weaponData.getNormalAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(i), weaponData.getNormalAmmo());
			weaponData.setWeaponState("normal");
		} else if (weaponData.getFireAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(i), weaponData.getFireAmmo());
			weaponData.setWeaponState("brand");
		} else if (weaponData.getExplosiveAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(i), weaponData.getExplosiveAmmo());
			weaponData.setWeaponState("explosiv");
		} else if (weaponData.getHeavyAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(i), weaponData.getHeavyAmmo());
			weaponData.setWeaponState("panzerbrechend");
		} else if (weaponData.getSpecialAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(i), weaponData.getSpecialAmmo());
			weaponData.setWeaponState("speziell");
		}
		weaponData.setSelected(false);
		
		addWeaponData(player, weaponData.getWeaponId(), weaponData);
	}
	
	private void SAVE_Weapons(Player player){
		for(int i=0; i<=46; i++){
		//	if(player.getWeaponData(WeaponModel.get(i).getSlot().getSlotId()) != null){
				WeaponData weaponData = getWeaponData(player, i);
				WeaponSystem.getInstance().getMysqlConnection().setWeapon(player, i, weaponData.isAble());
				WeaponSystem.getInstance().getMysqlConnection().setSelected(player, i, weaponData.isSelected());
				WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getNormalAmmo(), "normal");
				WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getFireAmmo(), "brand");
				WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getExplosiveAmmo(), "explosiv");
				WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getHeavyAmmo(), "panzerbrechend");
				WeaponSystem.getInstance().getMysqlConnection().setMUNI(player, i, weaponData.getSpecialAmmo(), "speziell");
		/*	}
			else {
				WeaponSystem.getInstance().getMysqlConnection().resetWeapon(player, i);
			}*/
		}
	}
	
	private void GIVE_NormalWeapons(Player player){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		playerLifecycle.setPlayerStatus("INIT");
		for(int i = 0; i <= 12; i++){
			//TODO: BUG ! Do it WITHOUT MYSQL, bcus if u give a weapon ingame, the weapon will not be load after dead or so on
			Integer weaponId = WeaponSystem.getInstance().getMysqlConnection().getWeapon(player, i);
			WeaponData weaponData = getWeaponData(player, weaponId);
			if(weaponId == 0 && i != 0){
				weaponId = player.getWeaponData(i).getModel().getId();
				weaponData = new WeaponData(player.getName(), weaponId);
				if(weaponData.getNormalAmmo() <= 0
				&& weaponData.getExplosiveAmmo() <= 0
				&& weaponData.getFireAmmo() <= 0
				&& weaponData.getHeavyAmmo() <= 0
				&& weaponData.getSpecialAmmo() <= 0){
					weaponData.setNormalAmmo(player.getWeaponData(i).getAmmo());
					weaponData.setWeaponState("normal");
				}
			}
			
			player.setWeaponAmmo(WeaponModel.get(weaponId), 0);
			UNSELECT_Weapons(player, i, "slot");
			weaponData.setSelected(true);
			weaponData.setAble(true);
			
			if (weaponData.getNormalAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getNormalAmmo());
				weaponData.setWeaponState("normal");
			} else if (weaponData.getFireAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getFireAmmo());
				weaponData.setWeaponState("brand");
			} else if (weaponData.getExplosiveAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getExplosiveAmmo());
				weaponData.setWeaponState("explosiv");
			} else if (weaponData.getHeavyAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getHeavyAmmo());
				weaponData.setWeaponState("panzerbrechend");
			} else if (weaponData.getSpecialAmmo() > 0) {
				player.giveWeapon(WeaponModel.get(weaponId), weaponData.getSpecialAmmo());
				weaponData.setWeaponState("speziell");
			}
			addWeaponData(player, weaponId, weaponData);
			if (isGun(weaponId)) SET_WeaponStatusText(player, weaponData.getWeaponState());
		}
		playerLifecycle.setPlayerStatus("INITED");
	}

	public void GIVE_PlayerExternWeapon(Player player, int weaponId){
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		WeaponData weaponData = getWeaponData(player, weaponId);
		if(weaponData.getWeaponId() == 0 && WeaponModel.get(weaponId).getSlot().getSlotId() != 0){
		//	weaponId = player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getModel().getId();
			weaponData = new WeaponData(player.getName(), weaponId);
			if(weaponData.getNormalAmmo() <= 0
			&& weaponData.getExplosiveAmmo() <= 0
			&& weaponData.getFireAmmo() <= 0
			&& weaponData.getHeavyAmmo() <= 0
			&& weaponData.getSpecialAmmo() <= 0){
				weaponData.setNormalAmmo(player.getWeaponData(WeaponModel.get(weaponId).getSlot().getSlotId()).getAmmo());
				weaponData.setWeaponState("normal");
			}
		}
		
		player.setWeaponAmmo(WeaponModel.get(weaponId), 0);
		UNSELECT_Weapons(player, weaponId, "weaponId");
		weaponData.setSelected(true);
		weaponData.setAble(true);
		
		if (weaponData.getNormalAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(weaponId), weaponData.getNormalAmmo());
			weaponData.setWeaponState("normal");
		} else if (weaponData.getFireAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(weaponId), weaponData.getFireAmmo());
			weaponData.setWeaponState("brand");
		} else if (weaponData.getExplosiveAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(weaponId), weaponData.getExplosiveAmmo());
			weaponData.setWeaponState("explosiv");
		} else if (weaponData.getHeavyAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(weaponId), weaponData.getHeavyAmmo());
			weaponData.setWeaponState("panzerbrechend");
		} else if (weaponData.getSpecialAmmo() > 0) {
			player.giveWeapon(WeaponModel.get(weaponId), weaponData.getSpecialAmmo());
			weaponData.setWeaponState("speziell");
		}
		addWeaponData(player, weaponId, weaponData);
		if (isGun(weaponId)) SET_WeaponStatusText(player, weaponData.getWeaponState());
	}
	/*
	private void CREATE_Explosion(Player player, Player victim, int countdown) {
		if(player != victim){
			CREATE_Countdown(countdown, victim, "BOOM");
			CREATE_Countdown(countdown, player, "BOOM");
			
			Timer timer = Timer.create(countdown*1000, (factualInterval) ->
			{
				WeaponsystemPlugin.getInstance().getShoebill().getSampObjectManager().getWorld().createExplosion(victim.getLocation(), 12, 1);
			});
			timer.start();
		}
		else {
			CREATE_Countdown(countdown, player, "BOOM");
		
			Timer timer = Timer.create(countdown*1000, (factualInterval) ->
			{
				WeaponsystemPlugin.getInstance().getShoebill().getSampObjectManager().getWorld().createExplosion(player.getLocation(), 12, 1);
			});
			timer.start();
		}
	}
	*/
	
	private void CREATE_WarnExplosion(Player player) {
		float health = player.getHealth();
		player.createExplosion(player.getLocation(), 12, 1);
		player.setHealth(health-5);
	}
	/*
	private void CREATE_Countdown(int countdown, Player player, String endText) {
		if(countdown>0){
			Timer timer = null;
			player.sendGameText(1000, 1, String.valueOf(countdown));
			countdown--;
			for(int i = countdown; i >= 0; i--){
				int ii = i;
				timer = Timer.create(1000, (factualInterval) ->
				{
					if(ii != 0){
						player.sendGameText(1000, 1, String.valueOf(ii));
					}
					else {
						player.sendGameText(1000, 1, endText);
					}
				});
				timer.start();
				timer.destroy();
			}
		}
	}
	*/
    public void addWeaponData(Player player, Integer key, WeaponData weaponData){
    	HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
		if (innerMap == null) innerMap = new HashMap<>();
		innerMap.put(key, weaponData);
		weaponDataMap.put(player, innerMap);
    }

    public WeaponData getWeaponData(Player player, Integer key){
        HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
        if(innerMap == null || !innerMap.containsKey(key)){
        //to import extern imported Weapons
			WeaponData weaponData = new WeaponData(player.getName(), key);

			//Initialize new Weapon
			weaponData.setNormalAmmo(WeaponModel.get(key)==player.getWeaponData(WeaponModel.get(key).getSlot().getSlotId()).getModel()?player.getWeaponData(WeaponModel.get(key).getSlot().getSlotId()).getAmmo():0);
			weaponData.setFireAmmo(0);
			weaponData.setExplosiveAmmo(0);
			weaponData.setHeavyAmmo(0);
			weaponData.setSpecialAmmo(0);
			weaponData.setWeaponState("normal");
			UNSELECT_Weapons(player, key, "weaponId");
			weaponData.setSelected(true);
			weaponData.setAble(true);

			//Add the weapon to the HashMap
			addWeaponData(player, key, weaponData);
        	return weaponData;
        }
        return innerMap.get(key);
    }

	public boolean hasWeaponData(Player player, Integer key){
        HashMap<Integer, WeaponData> innerMap = weaponDataMap.get(player);
		if (innerMap == null) innerMap = new HashMap<>();
		return innerMap.containsKey(key);
	}

    void UNSELECT_Weapons(Player player, int value, String typ) {
		for(int weaponId : WeaponSystem.getInstance().getMysqlConnection().getSlotWeapons(typ.equals("slot")?value:WeaponModel.get(value).getSlot().getSlotId())){
			WeaponData weaponData = getWeaponData(player, weaponId);
			weaponData.setSelected(false);
			addWeaponData(player, weaponData.getWeaponId(), weaponData);
		}
	}

	private void TogglePlayerBurning(Player player, boolean burning) {
		if(!IsPlayerInWater(player)){
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
			Timer timer = new Timer();
			Timer timer2 = new Timer();
    		if(burning)
	        {
    			PlayerObject.create(player, 18688, new Location(player.getLocation().x, player.getLocation().y, player.getLocation().z), 0, 0, 0);
    			playerLifecycle.setHealth(player.getHealth());
    			playerLifecycle.setPlayerStatus("ignited");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                    	BurningTimer(player);
                    }
                }, 0, 1000);
                timer2.schedule(new TimerTask() {
                    @Override
                    public void run() {
            			TogglePlayerBurning(player, false);
            			timer.cancel();
                    }
                }, 7000);
	        }
	        else
	        {
	        	PlayerObject.get(player, 18688).destroy();
				//	playerLifecycle.setWeaponState("normal");
				playerLifecycle.setPlayerStatus("normal");
			}
    	}
    }

    private boolean IsPlayerInWater(Player player){
    	float z = player.getLocation().z;
		return (1544 >= player.getAnimationIndex() && player.getAnimationIndex() >= 1538 || player.getAnimationIndex() == 1062 || player.getAnimationIndex() == 1250) && (z <= 0 || (z <= 41.0 && IsPlayerInArea(player, -1387, -473, 2025, 2824))) ||
				(1544 >= player.getAnimationIndex() && player.getAnimationIndex() >= 1538 || player.getAnimationIndex() == 1062 || player.getAnimationIndex() == 1250) && (z <= 2 || (z <= 39.0 && IsPlayerInArea(player, -1387, -473, 2025, 2824)));
	}

	private boolean IsPlayerInArea(Player player, float minX, float maxX, float minY, float maxY) {
		float x = player.getLocation().x;
    	float y = player.getLocation().y;
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}

	boolean isGun(int weaponId) {
		return WeaponModel.get(weaponId).getSlot().getSlotId() != 0
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 1
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 8
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 9
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 10
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 11
			&& WeaponModel.get(weaponId).getSlot().getSlotId() != 12;
	}

	private void BurningTimer(Player player){
    	float hp = player.getHealth();
    	player.setHealth(hp-2);
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
