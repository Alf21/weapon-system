package me.alf21.weaponsystem;

import java.io.IOException;

import net.gtaun.shoebill.common.dialog.AbstractDialog;
import net.gtaun.shoebill.common.dialog.InputDialog;
import net.gtaun.shoebill.common.dialog.ListDialog;
import net.gtaun.shoebill.common.dialog.ListDialogItem;
import net.gtaun.shoebill.common.dialog.MsgboxDialog;
import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.object.Player;
import net.gtaun.shoebill.*;

public class WeaponShop {
	public PlayerData playerLifecycle;
	
	public void Shop(Player player) throws IOException {
		Shoebill.get().runOnSampThread(() -> {
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
							                		weaponData.setNormalAmmo(WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())?0:1);
							                		weaponData.setExplosiveAmmo(0);
							                		weaponData.setFireAmmo(0);
							                		weaponData.setHeavyAmmo(0);
							                		weaponData.setSpecialAmmo(0);
							                		weaponData.setAble(true);
							                		WeaponSystem.getInstance().getPlayerManager().UNSELECT_Weapons(player, weaponData.getWeaponId(), "weaponId");
							                		weaponData.setSelected(true);
							                	//	weaponData.setWeaponState("normal");
							                		WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
							                		if(!WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())){
							                			weaponData.setNormalAmmo(1);
							                			weaponData.setWeaponState("normal");
								                		WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
								                		WeaponSystem.getInstance().getPlayerManager().GIVE_PlayerExternWeapon(player, weaponData.getWeaponId());
							                		} else {
							                			if(weaponData.getNormalAmmo() <= 0
							                			&& weaponData.getExplosiveAmmo() <= 0
							                			&& weaponData.getFireAmmo() <= 0
							                			&& weaponData.getHeavyAmmo() <= 0
							                			&& weaponData.getSpecialAmmo() <= 0)
							                				reloadWeapon(player, weaponData, shopDialog, weaponName3);
							                			else WeaponSystem.getInstance().getPlayerManager().GIVE_PlayerExternWeapon(player, weaponData.getWeaponId());
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
				                	} else if(weaponData.isAble() && weaponData.isSelected() && WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())){
		                				reloadWeapon(player, weaponData, shopDialog, weaponName1);
				                	} else {
				                		if(!WeaponSystem.getInstance().getPlayerManager().isGun(weaponData.getWeaponId())){
				                			weaponData.setNormalAmmo(1);
				                			weaponData.setWeaponState("normal");
					                		WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
					                		WeaponSystem.getInstance().getPlayerManager().GIVE_PlayerExternWeapon(player, weaponData.getWeaponId());
				                		} else {
				                			if(weaponData.getNormalAmmo() <= 0
				                			&& weaponData.getExplosiveAmmo() <= 0
				                			&& weaponData.getFireAmmo() <= 0
				                			&& weaponData.getHeavyAmmo() <= 0
				                			&& weaponData.getSpecialAmmo() <= 0)
				                				reloadWeapon(player, weaponData, shopDialog, weaponName1);
				                			else WeaponSystem.getInstance().getPlayerManager().GIVE_PlayerExternWeapon(player, weaponData.getWeaponId());
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
		});
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
        	for(String munityp : getAvailableMunityp(weaponData.getWeaponId())){
        		if(munityp == null) continue;
        		ready = true;
        		double ammoTypPrice = WeaponSystem.getInstance().getMysqlConnection().getAmmoPrice(weaponData.getWeaponId());
        		if(munityp.equals("normal")) ammoTypPrice = ammoTypPrice/100 * 100;
        		else if(munityp.equals("brand")) ammoTypPrice = ammoTypPrice/100 * 10;
        		else if(munityp.equals("explosiv")) ammoTypPrice = ammoTypPrice/100 * 250;
        		else if(munityp.equals("panzerbrechend")) ammoTypPrice = ammoTypPrice/100 * 120;
        		else if(munityp.equals("speziell")) ammoTypPrice = ammoTypPrice/100 * 350;
        		String text = "";
        		if(munityp != null && !munityp.equals("")) text = munityp.substring(0, 1).toUpperCase() + munityp.substring(1).toLowerCase();
        		
				int ammo = 1;
        		try {
        			ammo = Integer.parseInt(s);
        		} catch (Exception e){
        			player.sendMessage("Only Numbers!");
        			reloadWeapon(player, weaponData, shopDialog, weaponName1);
        		}
        		int ammoPrice2 = (int) (ammoTypPrice*ammo);
				int ammo2 = ammo;
        		
				ammoDialog.getItems().add(ListDialogItem.create()
	            .itemText("Amount (" + text + "): " + (ammoPrice2>player.getMoney()?"{FF0000}":"{00FF00}") + ammo + " / $" + ammoPrice2)
	            .onSelect((listDialogItem2, o2) -> {
                	try {
                	//	final int ammoPrice3 = ammoPrice2*ammo2;
                    //	if (ammoPrice3 < 0 || ammoPrice3 > player.getMoney())
                		if (ammoPrice2 < 0 || ammoPrice2 > player.getMoney())
                		{
                			player.sendMessage(Color.WHITE, "Not enough money.");
                			shopDialog.show();
                		}
                    	else {
                    		MsgboxDialog.create(player, WeaponSystem.getInstance().getEventManagerInstance())
							.caption("{FF8A05}Buy " + ammo2 + " AMMO") 
		                    .message("Do you want to buy " + ammo2 + " AMMO for $" + ammoPrice2) 
		                    .buttonCancel("Back") 
		                    .buttonOk("Get!") 
		                    .onClickOk((s1) -> {
		                    	try {
			                		player.setMoney(player.getMoney()-ammoPrice2);
			                		if(munityp.equals("normal")) weaponData.setNormalAmmo(weaponData.getNormalAmmo()+ammo2);
			                		else if(munityp.equals("brand")) weaponData.setFireAmmo(weaponData.getFireAmmo()+ammo2);
			                		else if(munityp.equals("explosiv")) weaponData.setExplosiveAmmo(weaponData.getExplosiveAmmo()+ammo2);
			                		else if(munityp.equals("panzerbrechend")) weaponData.setHeavyAmmo(weaponData.getHeavyAmmo()+ammo2);
			                		else if(munityp.equals("speziell")) weaponData.setSpecialAmmo(weaponData.getSpecialAmmo()+ammo2);
			                		weaponData.setWeaponState(munityp);
			                		WeaponSystem.getInstance().getPlayerManager().addWeaponData(player, weaponData.getWeaponId(), weaponData);
			                		WeaponSystem.getInstance().getPlayerManager().GIVE_PlayerExternWeapon(player, weaponData.getWeaponId());
			                		Shop(player);
		                    	} catch (Exception e){
		                    		System.out.println(e);
		                			e.printStackTrace();
		                    	}
		                    })
		                    .parentDialog(ammoDialog)
		                    .onClickCancel(AbstractDialog::showParentDialog) 
		                    .build() 
		                    .show();
                    	}
                	} catch (Exception e){
                		System.out.println(e);
            			e.printStackTrace();
                	}
                })
                .build());
            }
        	if(ready) ammoDialog.show();
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

	private String[] getAvailableMunityp(int weaponId){
		String[] array = {"normal", "brand", "explosiv", "panzerbrechend", "speziell"};
		return array;
	}
}
