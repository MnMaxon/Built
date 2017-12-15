package me.MnMaxon.Utils;

import java.sql.*;

/**
 * Created by MnMaxon on 2/3/2016.  Aren't I great?
 */
public class MySQL {
    private final String ip;
    private final String database;
    private final String username;
    private final String password;

    public MySQL(String ip, String database, String username, String password) throws SQLException {
        this.ip = ip;
        this.database = database;
        this.username = username;
        this.password = password;
        if (getConnection() == null) throw new SQLException();
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + ip + "/" + database, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void executePreparedStatement(String statement) {
        executePreparedStatement(statement, false);
    }


    public void executePreparedStatement(String statement, boolean ignoreError) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(statement);
            ps.execute();
        } catch (Exception ex) {
            if (!ignoreError) ex.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
            if (ps != null) ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ResultPack executePreparedQuery(String statement) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            return new ResultPack(conn, ps, ps.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addColumn(String table, String column, int size) {
        executePreparedStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " VARCHAR(" + size + ");", true);
    }

    public class ResultPack {
        private final Connection conn;
        private final PreparedStatement ps;
        private final ResultSet resultSet;

        public ResultPack(Connection conn, PreparedStatement ps, ResultSet resultSet) {
            this.conn = conn;
            this.ps = ps;
            this.resultSet = resultSet;
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        public void close() {
            try {
                rawClose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void rawClose() throws SQLException {
            conn.close();
            ps.close();
            resultSet.close();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            try {
                rawClose();
            } catch (Exception ignored) {
            }
        }
    }

    public int getInt(String table, String keyColumn, String key, String valueColumn) {
        MySQL.ResultPack rp = executePreparedQuery("SELECT * FROM " + table + " WHERE (" + keyColumn + "='" + key + "');");
        ResultSet rs = rp.getResultSet();
        int current = 0;
        try {
            if (rs.next()) current = rs.getInt(valueColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rp.close();
        return current;
    }

    public void set(String table, String keyColumn, String key, String valueColumn, String value) {
        int current = 0;
        MySQL.ResultPack rp = executePreparedQuery("SELECT * FROM " + table + " WHERE (" + keyColumn + "='" + key + "');");
        ResultSet rs = rp.getResultSet();
        try {
            if (rs.next())
                executePreparedStatement("UPDATE " + table + " SET " + valueColumn + " = '" + value + "' WHERE (" + keyColumn + "='" + key + "');");
            else
                executePreparedStatement("INSERT INTO " + table + " (" + keyColumn + ", " + valueColumn + ") VALUES ('" + key + "', '" + value + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rp.close();
    }
}
