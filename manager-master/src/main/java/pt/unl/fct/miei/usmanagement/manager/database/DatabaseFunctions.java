package pt.unl.fct.miei.usmanagement.manager.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings({"SqlResolve", "unused"})
public class DatabaseFunctions {

	public static void dropSymTables(Connection conn) throws SQLException {
		conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS=0");
		ResultSet rs = conn.createStatement().
			executeQuery("SELECT table_name FROM information_schema.tables WHERE table_name ilike 'sym_%'");
		while (rs.next()) {
			String tableName = rs.getString(1);
			conn.createStatement().executeUpdate("DROP TABLE IF EXISTS \"" + tableName + "\" CASCADE");
		}
		conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS=1");
	}

	public static void dropSymTriggers(Connection conn) throws SQLException {
		ResultSet rs = conn.createStatement().
			executeQuery("SELECT trigger_name FROM information_schema.triggers WHERE trigger_name ilike 'sym_%'");
		while (rs.next()) {
			String tableName = rs.getString(1);
			conn.createStatement().executeUpdate("DROP TRIGGER IF EXISTS \"" + tableName + "\"");
		}
	}

	public static void dropSymFunctions(Connection conn) throws SQLException {
		ResultSet rs = conn.createStatement().
			executeQuery("SELECT alias_name FROM information_schema.function_aliases WHERE alias_name ilike 'sym_%'");
		while (rs.next()) {
			String aliasName = rs.getString(1);
			conn.createStatement().executeUpdate("DROP ALIAS IF EXISTS \"" + aliasName + "\"");
		}
	}

}
