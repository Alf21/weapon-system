package me.alf21.weaponsystem;

import net.gtaun.shoebill.common.dialog.*;
import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.object.Player;

import java.io.IOException;

public class WeaponShop {
	public PlayerData playerLifecycle;
	
	public void shop(Player player) throws IOException {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		try {
			playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
			try {
				ListDialog shopDialog = ListDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
		                .caption("{FF8A05}Weaponshop")
		                .buttonOk("Next")
		                .buttonCancel("Exit") 
		                .build();
				
				for(int i=1; i<=46; i++){
					if(i==19||i==20||i==21) continue;
					if(i == 38 && !PlayerManager.allowMinigun) continue;
				//	if(i==37||WeaponModel.get(i).getSlot().getSlotId()==9) continue; //TODO: Flamethrower bugfix
					WeaponData weaponData = WeaponSystem.getInstance().getPlayerManager().getWeaponData(player, i);
					int price = WeaponSystem.getInstance().getMysqlConnection().getWeaponPrice(weaponData.getWeaponId());
                	String weaponName;
					try {
						weaponName = WeaponModel.get(weaponData.getWeaponId()).getName();
					} catch (Exception e){
						weaponName = "UNKNOWN";
					}
					shopDialog.getItems().add(ListDialogItem.create()
			            .itemText((weaponData.isAble()?weaponData.isSelected()?"{CCCCCC}":"{00FF00}":weaponData.isSelected()?"{CCCCCC}":"{FF0000}") + weaponName)
			            .onSelect((listDialogItem, o) -> {
			                try {
			                	String weaponName1;
								try {
									weaponName1 = WeaponModel.get(weaponData.getWeaponId()).getName();
								} catch (Exception e){
									weaponName1 = "UNKNOWN";
								}
			                	if(!weaponData.isAble() && weaponData.getWeaponId() != 0){
		                    		String weaponName3 = weaponName1;
				                	MsgboxDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
									.caption("{FF8A05}Buy weapon " + weaponName1) 
				                    .message("Do you want to buy " + weaponName1 + " for $" + price) 
				                    .buttonCancel("Cancel") 
				                    .buttonOk("Get!") 
				                    .onClickOk((s) -> {
				                    	try {
					                    	if (price < 0 || price > player.getMoney())
					                		{
					                			player.sendMessage(Color.WHITE, "Not enough money.");
					                			shopDialog.show();
					                		}
					                    	else {
						                		player.setMoney(player.getMoney()-price);
						                		weaponData.setNormalAmmo(
						                				WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())||
						                				WeaponSystem.getInstance().getPlayerManager().isRechargeable(weaponData.getWeaponId())?0:1);
						                		weaponData.setExplosiveAmmo(0);
						                		weaponData.setFireAmmo(0);
						                		weaponData.setHeavyAmmo(0);
						                		weaponData.setSpecialAmmo(0);
						                		weaponData.setAble(true);
						                		WeaponSystem.getInstance().getPlayerManager().unselectWeapons(player, weaponData.getWeaponId(), "weaponId");
						                		weaponData.setSelected(true);
												//	weaponData.setAmmoState("normal");
												WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
						                		if(!WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId()) && !WeaponSystem.getInstance().getPlayerManager().isRechargeable(weaponData.getWeaponId())){
						                			weaponData.setNormalAmmo(1);
													weaponData.setAmmoState(AmmoState.NORMAL);
													WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
							                		WeaponSystem.getInstance().getPlayerManager().givePlayerExternWeapon(player, weaponData.getWeaponId());
						                		} else {
						                			if(weaponData.getNormalAmmo() <= 0
						                			&& weaponData.getExplosiveAmmo() <= 0
						                			&& weaponData.getFireAmmo() <= 0
						                			&& weaponData.getHeavyAmmo() <= 0
						                			&& weaponData.getSpecialAmmo() <= 0)
						                				reloadWeapon(player, weaponData, shopDialog, weaponName3);
						                			else WeaponSystem.getInstance().getPlayerManager().givePlayerExternWeapon(player, weaponData.getWeaponId());
						                		}
					                    	}
				                    	} catch (Exception e){
				                    		System.out.println(e);
				                			e.printStackTrace();
				                    	}
				                    })
				                    .parentDialog(shopDialog)
				                    .onClickCancel(AbstractDialog::showParentDialog) 
				                    .build() 
				                    .show();
			                	} else if(weaponData.isAble() && weaponData.isSelected() && WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())
			                			||weaponData.isAble() && weaponData.isSelected() && WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId() == 9
			                			||weaponData.isAble() && weaponData.isSelected() && WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId() == 8){
	                				reloadWeapon(player, weaponData, shopDialog, weaponName1);
			                	} else {
			                		if(!WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId()) && !WeaponSystem.getInstance().getPlayerManager().isRechargeable(weaponData.getWeaponId())){
			                			weaponData.setNormalAmmo(1);
										weaponData.setAmmoState(AmmoState.NORMAL);
										WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
				                		WeaponSystem.getInstance().getPlayerManager().givePlayerExternWeapon(player, weaponData.getWeaponId());
			                		} else {
			                			if(weaponData.getNormalAmmo() <= 0
			                			&& weaponData.getExplosiveAmmo() <= 0
			                			&& weaponData.getFireAmmo() <= 0
			                			&& weaponData.getHeavyAmmo() <= 0
			                			&& weaponData.getSpecialAmmo() <= 0)
			                				reloadWeapon(player, weaponData, shopDialog, weaponName1);
			                			else WeaponSystem.getInstance().getPlayerManager().givePlayerExternWeapon(player, weaponData.getWeaponId());
			                		}
			                	}
			                } catch (Exception e) {
								System.out.println(e);
								e.printStackTrace();
							}
			            })
			            .build());
				}
				
				shopDialog.show();
			} catch (Exception e) {
				System.out.println(e);
				player.sendMessage(Color.RED, "An error occupied, so please reconnect or ask our Support!");
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println(e);
			player.sendMessage(Color.RED, "An error occupied, so please reconnect or ask our Support!");
			e.printStackTrace();
		}
	}
	
	private void reloadWeapon(Player player, WeaponData weaponData, ListDialog shopDialog, String weaponName1) {
		playerLifecycle = WeaponSystem.getInstance().getPlayerLifecycleHolder().getObject(player, PlayerData.class);
		
		InputDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
		.caption("{FF8A05}Reload weapon " + weaponName1) 
        .message("Do you want to buy AMMO for " + weaponName1) 
        .buttonCancel("Back") 
        .buttonOk("Next!") 
        .onClickOk((inputDialog, s) -> {
        	ListDialog ammoDialog = ListDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
            .caption("{FF8A05}Weaponshop")
            .buttonOk("Buy!")
            .buttonCancel("Back") 
            .parentDialog(inputDialog)
            .onClickCancel(AbstractDialog::showParentDialog) 
            .build();
        	boolean ready = false;
			boolean error = false;
			for (AmmoState munityp : AmmoState.values()) {
				if(munityp == null) continue;
				if(munityp != AmmoState.NORMAL && WeaponSystem.getInstance().getPlayerManager().isRechargeable(weaponData.getWeaponId())
				|| munityp != AmmoState.NORMAL && !WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())
				|| munityp != AmmoState.NORMAL && WeaponModel.get(weaponData.getWeaponId()).getSlot().getSlotId() == 7 && weaponData.getWeaponId() != 38)
					continue;
        		ready = true;
        		double ammoTypPrice = WeaponSystem.getInstance().getMysqlConnection().getAmmoPrice(weaponData.getWeaponId());
				if (munityp == AmmoState.NORMAL) ammoTypPrice = ammoTypPrice / 100 * 100;
				else if (munityp == AmmoState.FIRE) ammoTypPrice = ammoTypPrice / 100 * 10;
				else if (munityp == AmmoState.EXPLOSIVE) ammoTypPrice = ammoTypPrice / 100 * 250;
				else if (munityp == AmmoState.HEAVY) ammoTypPrice = ammoTypPrice / 100 * 120;
				else if (munityp == AmmoState.SPECIAL) ammoTypPrice = ammoTypPrice / 100 * 350;

				int ammo = 1;
        		try {
        			ammo = Integer.parseInt(s);
        		} catch (Exception e){
        			error = true;
        			player.sendMessage("Only Numbers!");
        		}
        		if(!error){
	        		int ammoPrice2 = (int) (ammoTypPrice*ammo);
					int ammo2 = ammo;
	        		
					ammoDialog.getItems().add(ListDialogItem.create()
							.itemText("Amount (" + munityp.getDisplayName() + "): " + (ammoPrice2 > player.getMoney() ? "{FF0000}" : "{00FF00}") + ammo + " / $" + ammoPrice2)
							.onSelect((listDialogItem2, o2) -> {
								try {
									//	final int ammoPrice3 = ammoPrice2*ammo2;
									//	if (ammoPrice3 < 0 || ammoPrice3 > player.getMoney())
									if (ammoPrice2 < 0 || ammoPrice2 > player.getMoney()) {
										player.sendMessage(Color.WHITE, "Not enough money.");
										shopDialog.show();
									} else {
										MsgboxDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
												.caption("{FF8A05}Buy " + ammo2 + " AMMO")
												.message("Do you want to buy " + ammo2 + " AMMO for $" + ammoPrice2)
												.buttonCancel("Back")
												.buttonOk("Get!")
												.onClickOk((s1) -> {
													try {
														player.setMoney(player.getMoney() - ammoPrice2);
														if (munityp == AmmoState.NORMAL)
															weaponData.setNormalAmmo(weaponData.getNormalAmmo() + weaponData.getWeaponId()==37?ammo2*10:ammo2);
														else if (munityp == AmmoState.FIRE)
															weaponData.setFireAmmo(weaponData.getFireAmmo() + weaponData.getWeaponId()==37?ammo2*10:ammo2);
														else if (munityp == AmmoState.EXPLOSIVE)
															weaponData.setExplosiveAmmo(weaponData.getExplosiveAmmo() + weaponData.getWeaponId()==37?ammo2*10:ammo2);
														else if (munityp == AmmoState.HEAVY)
															weaponData.setHeavyAmmo(weaponData.getHeavyAmmo() + weaponData.getWeaponId()==37?ammo2*10:ammo2);
														else if (munityp == AmmoState.SPECIAL)
															weaponData.setSpecialAmmo(weaponData.getSpecialAmmo() + weaponData.getWeaponId()==37?ammo2*10:ammo2);
														weaponData.setAmmoState(munityp);
														WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
														WeaponSystem.getInstance().getPlayerManager().givePlayerExternWeapon(player, weaponData.getWeaponId());
														this.shop(player);
													} catch (Exception e) {
														e.printStackTrace();
													}
												})
												.parentDialog(ammoDialog)
												.onClickCancel(AbstractDialog::showParentDialog)
												.build()
												.show();
									}
								} catch (Exception e) {
									System.out.println(e);
									e.printStackTrace();
								}
							})
	                .build());
        		} else {
        			reloadWeapon(player, weaponData, shopDialog, weaponName1);
        			break;
        		}
            }
        	if(ready){
        		if(!error) ammoDialog.show();
        	}
        	else {
        		MsgboxDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
				.caption("{FF0000}No Ammo available!") 
                .message("Currently there is no ammo available for your Weapon " + weaponName1 + "! :(") 
                .buttonCancel("Back") 
                .buttonOk("Ok :(") 
                .parentDialog(shopDialog)
                .onClickOk(AbstractDialog::showParentDialog)
                .onClickCancel(AbstractDialog::showParentDialog) 
                .build() 
                .show();
        	}
    	})
        .parentDialog(shopDialog)
        .onClickCancel(AbstractDialog::showParentDialog) 
        .build() 
        .show();
	}
}
