package me.alf21.weaponsystem;

import net.gtaun.shoebill.object.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;

/**
 * Created by Marvin on 26.05.2014.
 * Edited & Added Funktions by Alf21.
 */
public class MysqlConnection {
    private boolean initialized;
    private Connection connection;
    public boolean initConnection() {
        if(!initialized) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                File fl = new File(WeaponSystem.getInstance().getDataDir(), "mysql.txt");
                if (fl.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(fl));
                    String line;
                    while (reader.ready()) {
                        line = reader.readLine();
                        String[] parts = line.split("[,]");
                        if (line.length() > 4) {
                            if (parts.length == 4)
                                connection = DriverManager.getConnection("jdbc:mysql://" + parts[0] + "/" + parts[1], parts[2], parts[3]);
                            else if (parts.length == 3)
                                connection = DriverManager.getConnection("jdbc:mysql://" + parts[0] + "/" + parts[1], parts[2], null);
                            initialized = true;
                            break;
                        }
                    }
                    reader.close();
                } else {
                    fl.createNewFile();
                    WeaponSystem.getInstance().getLoggerInstance().info("[Fehler] Die Mysql Datei, wurde so eben erst erstellt!");
                    WeaponSystem.getInstance().getShoebill().getSampObjectManager().getServer().sendRconCommand("exit");
                    return false;
                }
            } catch (Exception ex) {
                WeaponSystem.getInstance().getLoggerInstance().info("[Fehler] Verbindung zum MysqlServer konnte nicht hergestellt werden!");
                WeaponSystem.getInstance().getShoebill().getSampObjectManager().getServer().sendRconCommand("exit");
                return false;
            }
        }
        return true;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeDatabase() {
        try {
            Statement stmnt = connection.createStatement();
            if (connection != null && connection.isValid(1000)) {
            	//TODO: AmmoPrice und WeaponPrice in eine andere GLOBALE Tabelle !
                String query = "CREATE TABLE IF NOT EXISTS samp_weaponsystem (Id INTEGER PRIMARY KEY AUTO_INCREMENT, player CHAR(24), rank INTEGER NOT NULL DEFAULT '1', ";
                for(int i=0;i<=46;i++){
                	//TODO: Table for Muni and Weapon prices, if buyed, set price to playerweapon and then on buy / 2 (-> dynamic prices)
                	//TODO: Shop, if buyed Slot 3 ammo, give ammo to all weapons, if buyed slot 12 ammo, only for every gun !
                	query += String.format("weapon%d_model BOOL NOT NULL DEFAULT '0', " +
           				 "weapon%d_skill INTEGER NOT NULL DEFAULT '0', " +
           				 "weapon%d_price INTEGER NOT NULL DEFAULT '0', " +
           				 "weapon%d_ammoprice INTEGER NOT NULL DEFAULT '0', " +
           				 "weapon%d_selected BOOL NOT NULL DEFAULT '0', " +
           				 "ammo%d_normal INTEGER NOT NULL DEFAULT '0', " +
           				 "ammo%d_explosiv INTEGER NOT NULL DEFAULT '0', " +
           				 "ammo%d_panzerbrechend INTEGER NOT NULL DEFAULT '0', " + 
           				 "ammo%d_brand INTEGER NOT NULL DEFAULT '0', ", i,i,i,i,i,i,i,i,i);
			         //  "weapon%d_status FLOAT NOT NULL DEFAULT '100'" + //TODO: falls man am Arm getroffen wurde, Waffenleben berechnen -> Daraus Treffgenauigkeit, Schüsse ohne Kugeln usw berechnen
                	if(i!=46){
                		query += String.format("ammo%d_speziell INTEGER NOT NULL DEFAULT '0', ", i);
                	}
                	else {
                		query += String.format("ammo%d_speziell INTEGER NOT NULL DEFAULT '0')", i);
                	}
                }
                stmnt.executeUpdate(query);
                
                query = "CREATE TABLE IF NOT EXISTS samp_weaponshop (weaponId INTEGER PRIMARY KEY AUTO_INCREMENT, weaponPrice INTEGER NOT NULL DEFAULT '0', ammoPrice INTEGER NOT NULL DEFAULT '0')";
                stmnt.executeUpdate(query);

                for(int i=1;i<=47;i++){
	                query = String.format("SELECT * FROM samp_weaponshop WHERE weaponId = '%d'", i);
	                try {
	                    if (connection != null && connection.isValid(1000)) {
	                        ResultSet rs = stmnt.executeQuery(query);
	        				if(!rs.first()){
	        	                stmnt.executeUpdate(String.format("INSERT INTO samp_weaponshop (weaponId, weaponPrice, ammoPrice) VALUES ('%d', '%d', '%d')", i, getNormalWeaponPrice(i-1), getNormalAmmoPrice(i-1)));
	        				}
	                    }
	        		} catch (SQLException e) {
	                    System.out.print("ERROR - Stacktrace : ");
	                    e.printStackTrace();
	                }
                }
    		} else {
                WeaponSystem.getInstance().getLoggerInstance().info("Mysql Datenbank konnte nicht erstellt werden.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public int executeUpdate(String query) {
        try {
            if (connection != null && connection.isValid(1000)) {
                Statement stmnt = connection.createStatement();
                stmnt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                try {
                    ResultSet rs = stmnt.getGeneratedKeys();
                    rs.next();
                    return rs.getInt(1);
                } catch (Exception ignored) { }
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ResultSet executeQuery(String query) {
        ResultSet rs = null;
        Statement statement;
        try {
            if (connection != null && connection.isValid(1000)) {
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
	
	public boolean check(String table, String field, String str) {
        ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM %s WHERE %s = '%s'", table, field, str);
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					try {
						if(!rs.getString(field).toLowerCase().equals(str.toLowerCase())){
							return true;
						}
					} catch (Exception e){
						return true;
					}
				}
            }
			else {
				return false;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return false;   
	}

	public Integer getWeapon(Player player, int slot) {
        ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM samp_weaponsystem WHERE player = '%s'", player.getName());
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					for (int weaponId : getSlotWeapons(slot)){
						if(rs.getBoolean("weapon"+weaponId+"_selected"))
							return weaponId;
					}
					//else
					for (int weaponId : getSlotWeapons(slot)){
						if(rs.getInt("ammo"+weaponId+"_normal") > 0
						|| rs.getInt("ammo"+weaponId+"_explosiv") > 0
						|| rs.getInt("ammo"+weaponId+"_panzerbrechend") > 0
						|| rs.getInt("ammo"+weaponId+"_brand") > 0
						|| rs.getInt("ammo"+weaponId+"_speziell") > 0)
							return weaponId;
					}
				}
            }
			else {
				return 0;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return 0;   
	}

	public boolean isAble(Player player, int weaponId) {
        ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM samp_weaponsystem WHERE player = '%s'", player.getName());
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getBoolean("weapon"+weaponId+"_model");
				}
            }
			else {
				return false;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return false;   
	}

	public int getMUNI(Player player, int i, AmmoState typ) {
		ResultSet rs = null;
		Statement statement;
        String query = String.format("SELECT * FROM samp_weaponsystem WHERE player = '%s'", player.getName());
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getInt("ammo" + i + "_" + typ);
				}
            }
			else {
				return 0;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return 0;   
	}

	public boolean exist(Player player) {
		ResultSet rs = null;
		Statement statement;
        String query = String.format("SELECT * FROM samp_weaponsystem WHERE player = '%s'", player.getName());
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return true;
				}
            }
			else {
				return false;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return false;
	}

	public void create(Player player) {
		try {
			if (connection != null && connection.isValid(1000)) {
				executeUpdate("INSERT INTO samp_weaponsystem (player, weapon30_model, ammo30_explosiv, ammo30_normal) VALUES ('"+player.getName()+"', '1', '6', '5')");
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
		}
	}

	public void setMUNI(Player player, Integer weaponId, int muni, AmmoState typ) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute(String.format("UPDATE samp_weaponsystem SET ammo%d_%s = '%d' WHERE player = '%s'",
						weaponId, typ.toString(), muni, player.getName()));
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}

	public void setWeapon(Player player, int weaponId, boolean bool) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute(String.format("UPDATE samp_weaponsystem SET weapon%d_model = '%d' WHERE player = '%s'", weaponId, bool?1:0, player.getName()));
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}

	public void setSelected(Player player, int weaponId, boolean bool) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute(String.format("UPDATE samp_weaponsystem SET weapon%d_selected = '%d' WHERE player = '%s'", weaponId, bool?1:0, player.getName()));
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}
	
	public boolean getSelected(Player player, int weaponId) {
        ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM samp_weaponsystem WHERE player = '%s'", player.getName());
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getBoolean("weapon"+weaponId+"_selected");
				}
            }
			else {
				return false;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return false;
	}
	
	public int getWeaponPrice(int weaponId){
		ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM samp_weaponshop WHERE weaponId = '%d'", weaponId+1);
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getInt("weaponPrice");
				}
            }
			else {
				return 0;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return 0;
	}
	
	public int getAmmoPrice(int weaponId){
		ResultSet rs;
        Statement statement;
        String query = String.format("SELECT * FROM samp_weaponshop WHERE weaponId = '%d'", weaponId+1);
        try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getInt("ammoPrice");
				}
            }
			else {
				return 0;
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
		return 0;
	}

	public void resetWeapon(Player player, int weaponId) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute(String.format("UPDATE samp_weaponsystem SET weapon%d_model = 'false', weapon_selected = 'false', ammo%d_normal = '0', ammo%d_explosiv ='0', ammo%d_brand = '0', ammo%d_panzerbrechend = '0', ammo%d_speziell = '0' WHERE player = '%s'", weaponId, weaponId, weaponId, weaponId, weaponId, weaponId, weaponId, player.getName()));
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}
	
	public int[] getSlotWeapons(int slot){
		if(slot == 0){
			int[] weapons = {0,1};
			return weapons;
		} else if(slot == 1){
			int[] weapons = {2,3,4,5,6,7,8,9};
			return weapons;
		} else if(slot == 2){
			int[] weapons = {22,23,24};
			return weapons;
		} else if(slot == 3){
			int[] weapons = {25,26,27};
			return weapons;
		} else if(slot == 4){
			int[] weapons = {28,29};
			return weapons;
		} else if(slot == 5){
			int[] weapons = {30,31};
			return weapons;
		} else if(slot == 6){
			int[] weapons = {33,34};
			return weapons;
		} else if(slot == 7){
			int[] weapons = {35,36,37,38};
			return weapons;
		} else if(slot == 8){
			int[] weapons = {39};
			return weapons;
		} else if(slot == 9){
			int[] weapons = {41,42,43};
			return weapons;
		} else if(slot == 10){
			int[] weapons = {10,11,12,13,14,15};
			return weapons;
		} else if(slot == 11){
			int[] weapons = {44,45,46};
			return weapons;
		} else {
			int[] weapons = {40};
			return weapons;
		}
	}

    private int getNormalWeaponPrice(int weaponId) {
    	if(weaponId == 0) return 0;
    	else if(weaponId == 1) return 180;
    	else if(weaponId == 2) return 120;
    	else if(weaponId == 3) return 80;
    	else if(weaponId == 4) return 160;
    	else if(weaponId == 5) return 240;
    	else if(weaponId == 6) return 90;
    	else if(weaponId == 7) return 160;
    	else if(weaponId == 8) return 520;
    	else if(weaponId == 9) return 1600;
    	else if(weaponId == 10) return 40;
    	else if(weaponId == 11) return 35;
    	else if(weaponId == 12) return 55;
    	else if(weaponId == 13) return 85;
    	else if(weaponId == 14) return 30;
    	else if(weaponId == 15) return 10;
    	else if(weaponId == 16) return 1000;
    	else if(weaponId == 17) return 0;
    	else if(weaponId == 18) return 0;
    	else if(weaponId == 19) return 0;
    	else if(weaponId == 20) return 0;
    	else if(weaponId == 21) return 0;
    	else if(weaponId == 22) return 800;
    	else if(weaponId == 23) return 1200;
    	else if(weaponId == 24) return 1500;
    	else if(weaponId == 25) return 2100;
    	else if(weaponId == 26) return 3800;
    	else if(weaponId == 27) return 3200;
    	else if(weaponId == 28) return 1900;
    	else if(weaponId == 29) return 2200;
    	else if(weaponId == 30) return 2400;
    	else if(weaponId == 31) return 2300;
    	else if(weaponId == 32) return 3100;
    	else if(weaponId == 33) return 4300;
    	else if(weaponId == 34) return 6500;
    	else if(weaponId == 35) return 8200;
    	else if(weaponId == 36) return 9400;
    	else if(weaponId == 37) return 6300;
    	else if(weaponId == 38) return 12000;
    	else if(weaponId == 39) return 2600;
    	else if(weaponId == 40) return 800;
    	else if(weaponId == 41) return 750;
    	else if(weaponId == 42) return 450;
    	else if(weaponId == 43) return 600;
    	else if(weaponId == 44) return 3200;
    	else if(weaponId == 45) return 3200;
    	else if(weaponId == 46) return 400;
    	else return 0;
	}

	private int getNormalAmmoPrice(int weaponId) {
    	if(weaponId == 0) return 0;
    	else if(weaponId == 1) return 0;
    	else if(weaponId == 2) return 0;
    	else if(weaponId == 3) return 0;
    	else if(weaponId == 4) return 0;
    	else if(weaponId == 5) return 0;
    	else if(weaponId == 6) return 0;
    	else if(weaponId == 7) return 0;
    	else if(weaponId == 8) return 0;
    	else if(weaponId == 9) return 0;
    	else if(weaponId == 10) return 0;
    	else if(weaponId == 11) return 0;
    	else if(weaponId == 12) return 0;
    	else if(weaponId == 13) return 0;
    	else if(weaponId == 14) return 0;
    	else if(weaponId == 15) return 0;
    	else if(weaponId == 16) return 2500;
    	else if(weaponId == 17) return 1500;
    	else if(weaponId == 18) return 800;
    	else if(weaponId == 19) return 0;
    	else if(weaponId == 20) return 0;
    	else if(weaponId == 21) return 0;
    	else if(weaponId == 22) return 15;
    	else if(weaponId == 23) return 15;
    	else if(weaponId == 24) return 15;
    	else if(weaponId == 25) return 45;
    	else if(weaponId == 26) return 45;
    	else if(weaponId == 27) return 45;
    	else if(weaponId == 28) return 8;
    	else if(weaponId == 29) return 8;
    	else if(weaponId == 30) return 8;
    	else if(weaponId == 31) return 8;
    	else if(weaponId == 32) return 8;
    	else if(weaponId == 33) return 24;
    	else if(weaponId == 34) return 24;
    	else if(weaponId == 35) return 300;
    	else if(weaponId == 36) return 350;
    	else if(weaponId == 37) return 2;
    	else if(weaponId == 38) return 8;
    	else if(weaponId == 39) return 500;
    	else if(weaponId == 40) return 0;
    	else if(weaponId == 41) return 1;
    	else if(weaponId == 42) return 1;
    	else if(weaponId == 43) return 0;
    	else if(weaponId == 44) return 0;
    	else if(weaponId == 45) return 0;
    	else if(weaponId == 46) return 0;
    	else return 0;
	}
}
