package me.alf21.weapon_system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;

import net.gtaun.shoebill.constant.WeaponModel;
import net.gtaun.shoebill.object.Player;

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
                File fl = new File(WeaponsystemPlugin.getInstance().getDataDir(), "mysql.txt");
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
                    WeaponsystemPlugin.getInstance().getLoggerInstance().info("[Fehler] Die Mysql Datei, wurde so eben erst erstellt!");
                    WeaponsystemPlugin.getInstance().getShoebill().getSampObjectManager().getServer().sendRconCommand("exit");
                    return false;
                }
            } catch (Exception ex) {
            	WeaponsystemPlugin.getInstance().getLoggerInstance().info("[Fehler] Verbindung zum MysqlServer konnte nicht hergestellt werden!");
                WeaponsystemPlugin.getInstance().getShoebill().getSampObjectManager().getServer().sendRconCommand("exit");
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
                stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS samp_weaponsystem (Id INTEGER PRIMARY KEY AUTO_INCREMENT, player CHAR(24), rank INTEGER NOT NULL DEFAULT '1', " + 
						            "weapon0_model INTEGER NOT NULL DEFAULT '0', weapon0_skill INTEGER NOT NULL DEFAULT '0', weapon0_price INTEGER NOT NULL DEFAULT '0', weapon0_ammoprice INTEGER NOT NULL DEFAULT '0', ammo0_normal INTEGER NOT NULL DEFAULT '0', ammo0_explosiv INTEGER NOT NULL DEFAULT '0', ammo0_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo0_brand INTEGER NOT NULL DEFAULT '0', ammo0_speziell INTEGER NOT NULL DEFAULT '0', " + 
						            "weapon1_model INTEGER NOT NULL DEFAULT '0', weapon1_skill INTEGER NOT NULL DEFAULT '0', weapon1_price INTEGER NOT NULL DEFAULT '0', weapon1_ammoprice INTEGER NOT NULL DEFAULT '0', ammo1_normal INTEGER NOT NULL DEFAULT '0', ammo1_explosiv INTEGER NOT NULL DEFAULT '0', ammo1_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo1_brand INTEGER NOT NULL DEFAULT '0', ammo1_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon2_model INTEGER NOT NULL DEFAULT '0', weapon2_skill INTEGER NOT NULL DEFAULT '0', weapon2_price INTEGER NOT NULL DEFAULT '0', weapon2_ammoprice INTEGER NOT NULL DEFAULT '0', ammo2_normal INTEGER NOT NULL DEFAULT '0', ammo2_explosiv INTEGER NOT NULL DEFAULT '0', ammo2_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo2_brand INTEGER NOT NULL DEFAULT '0', ammo2_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon3_model INTEGER NOT NULL DEFAULT '0', weapon3_skill INTEGER NOT NULL DEFAULT '0', weapon3_price INTEGER NOT NULL DEFAULT '0', weapon3_ammoprice INTEGER NOT NULL DEFAULT '0', ammo3_normal INTEGER NOT NULL DEFAULT '0', ammo3_explosiv INTEGER NOT NULL DEFAULT '0', ammo3_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo3_brand INTEGER NOT NULL DEFAULT '0', ammo3_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon4_model INTEGER NOT NULL DEFAULT '0', weapon4_skill INTEGER NOT NULL DEFAULT '0', weapon4_price INTEGER NOT NULL DEFAULT '0', weapon4_ammoprice INTEGER NOT NULL DEFAULT '0', ammo4_normal INTEGER NOT NULL DEFAULT '0', ammo4_explosiv INTEGER NOT NULL DEFAULT '0', ammo4_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo4_brand INTEGER NOT NULL DEFAULT '0', ammo4_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon5_model INTEGER NOT NULL DEFAULT '0', weapon5_skill INTEGER NOT NULL DEFAULT '0', weapon5_price INTEGER NOT NULL DEFAULT '0', weapon5_ammoprice INTEGER NOT NULL DEFAULT '0', ammo5_normal INTEGER NOT NULL DEFAULT '0', ammo5_explosiv INTEGER NOT NULL DEFAULT '0', ammo5_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo5_brand INTEGER NOT NULL DEFAULT '0', ammo5_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon6_model INTEGER NOT NULL DEFAULT '0', weapon6_skill INTEGER NOT NULL DEFAULT '0', weapon6_price INTEGER NOT NULL DEFAULT '0', weapon6_ammoprice INTEGER NOT NULL DEFAULT '0', ammo6_normal INTEGER NOT NULL DEFAULT '0', ammo6_explosiv INTEGER NOT NULL DEFAULT '0', ammo6_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo6_brand INTEGER NOT NULL DEFAULT '0', ammo6_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon7_model INTEGER NOT NULL DEFAULT '0', weapon7_skill INTEGER NOT NULL DEFAULT '0', weapon7_price INTEGER NOT NULL DEFAULT '0', weapon7_ammoprice INTEGER NOT NULL DEFAULT '0', ammo7_normal INTEGER NOT NULL DEFAULT '0', ammo7_explosiv INTEGER NOT NULL DEFAULT '0', ammo7_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo7_brand INTEGER NOT NULL DEFAULT '0', ammo7_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon8_model INTEGER NOT NULL DEFAULT '0', weapon8_skill INTEGER NOT NULL DEFAULT '0', weapon8_price INTEGER NOT NULL DEFAULT '0', weapon8_ammoprice INTEGER NOT NULL DEFAULT '0', ammo8_normal INTEGER NOT NULL DEFAULT '0', ammo8_explosiv INTEGER NOT NULL DEFAULT '0', ammo8_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo8_brand INTEGER NOT NULL DEFAULT '0', ammo8_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon9_model INTEGER NOT NULL DEFAULT '0', weapon9_skill INTEGER NOT NULL DEFAULT '0', weapon9_price INTEGER NOT NULL DEFAULT '0', weapon9_ammoprice INTEGER NOT NULL DEFAULT '0', ammo9_normal INTEGER NOT NULL DEFAULT '0', ammo9_explosiv INTEGER NOT NULL DEFAULT '0', ammo9_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo9_brand INTEGER NOT NULL DEFAULT '0', ammo9_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon10_model INTEGER NOT NULL DEFAULT '0', weapon10_skill INTEGER NOT NULL DEFAULT '0', weapon10_price INTEGER NOT NULL DEFAULT '0', weapon10_ammoprice INTEGER NOT NULL DEFAULT '0', ammo10_normal INTEGER NOT NULL DEFAULT '0', ammo10_explosiv INTEGER NOT NULL DEFAULT '0', ammo10_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo10_brand INTEGER NOT NULL DEFAULT '0', ammo10_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon11_model INTEGER NOT NULL DEFAULT '0', weapon11_skill INTEGER NOT NULL DEFAULT '0', weapon11_price INTEGER NOT NULL DEFAULT '0', weapon11_ammoprice INTEGER NOT NULL DEFAULT '0', ammo11_normal INTEGER NOT NULL DEFAULT '0', ammo11_explosiv INTEGER NOT NULL DEFAULT '0', ammo11_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo11_brand INTEGER NOT NULL DEFAULT '0', ammo11_speziell INTEGER NOT NULL DEFAULT '0', " +
						            "weapon12_model INTEGER NOT NULL DEFAULT '0', weapon12_skill INTEGER NOT NULL DEFAULT '0', weapon12_price INTEGER NOT NULL DEFAULT '0', weapon12_ammoprice INTEGER NOT NULL DEFAULT '0', ammo12_normal INTEGER NOT NULL DEFAULT '0', ammo12_explosiv INTEGER NOT NULL DEFAULT '0', ammo12_panzerbrechend INTEGER NOT NULL DEFAULT '0', ammo12_brand INTEGER NOT NULL DEFAULT '0', ammo12_speziell INTEGER NOT NULL DEFAULT '0'" +
						         //   "weapon0_status FLOAT NOT NULL DEFAULT '100'" + //TODO:falls man am Arm getroffen wurde, Waffenleben berechnen -> Daraus Treffgenauigkeit, Schüsse ohne Kugeln usw berechnen
						            ")");
            } else {
            	WeaponsystemPlugin.getInstance().getLoggerInstance().info("Mysql Datenbank konnte nicht erstellt werden.");
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
		ResultSet rs = null;
		Statement statement;
		String query = "SELECT * FROM "+table+" WHERE "+field+" = '"+str+"'";
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

	public Integer getWeapon(Player player, int i) {
		ResultSet rs = null;
		Statement statement;
		String query = "SELECT * FROM samp_weaponsystem WHERE player = '"+player.getName()+"'";
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                rs = statement.executeQuery(query);
				if(rs.first()){
					return rs.getInt("weapon" + i + "_model");
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

	public int getMUNI(Player player, int i, String typ) {
		ResultSet rs = null;
		Statement statement;
		String query = "SELECT * FROM samp_weaponsystem WHERE player = '"+player.getName()+"'";
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
		String query = "SELECT * FROM samp_weaponsystem WHERE player = '" + player.getName() + "'";
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
				executeUpdate("INSERT INTO samp_weaponsystem (player, weapon5_model, ammo5_explosiv, ammo5_normal) VALUES ('"+player.getName()+"', '30', '6', '5')");
			}
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
		}
	}

	public void setMUNI(Player player, Integer weaponId, int muni, String typ) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute("UPDATE samp_weaponsystem SET ammo"+WeaponModel.get(weaponId).getSlot().getSlotId()+"_"+typ+" = '"+muni+"' WHERE player = '"+player.getName()+"'");
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}

	public void setWeapon(Player player, int slot, int weaponId) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute("UPDATE samp_weaponsystem SET weapon"+slot+"_model = '"+weaponId+"' WHERE player = '"+player.getName()+"'");
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}

	public void resetWeaponSlot(Player player, int slot) {
		Statement statement;
		try {
            if (connection != null && connection.isValid(1000)) {
            	statement = connection.createStatement();
                statement.execute("UPDATE samp_weaponsystem SET weapon"+slot+"_model = '0', ammo"+slot+"_normal = '0', ammo"+slot+"_explosiv ='0', ammo"+slot+"_brand = '0', ammo"+slot+"_panzerbrechend = '0', ammo"+slot+"_speziell = '0' WHERE player = '"+player.getName()+"'");
            }
		} catch (SQLException e) {
            System.out.print("ERROR - Stacktrace : ");
            e.printStackTrace();
        }
	}
}
