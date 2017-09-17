package com.mad.bank.server;

import com.mad.bank.common.OperationRecord;
import com.mad.bank.server.UserClasses.BankAccount;
import com.mad.bank.server.UserClasses.BankCustomer;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class DBInitializer {
    private final String DRIVER;
    private final String DB_URL;
    private static Connection conn = null;
    private static Statement stmt = null;
    private Map<String, BankCustomer> customers = new HashMap<>();
    private Map<String, BankAccount> accounts = new HashMap<>();
    private TreeSet<OperationRecord> oprs = new TreeSet<>();

    DBInitializer(String driver, String url) {
        DRIVER = driver;
        DB_URL = url;
        try {
            this.customers = getCredentials();
            assignAccounts();
            assignOperations();
        } catch (ClassNotFoundException|SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, BankCustomer> getCustomers() {
        return customers;
    }

    public void assignAccounts() throws SQLException, ClassNotFoundException {
        this.accounts = getAccounts();
        Set<Map.Entry<String, BankAccount>> entrySet = accounts.entrySet();
        for (Map.Entry<String, BankAccount> entry : entrySet) {
            try {
                customers.get(entry.getValue().getUserID()).putAccount(entry.getValue());
                System.out.println(entry.getValue());
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void assignOperations() throws SQLException, ClassNotFoundException {
        this.oprs = getOperationRecords();
        for (OperationRecord opr : oprs) {
            try {
                accounts.get(opr.getAccountID()).addOperation(opr);
                System.out.println(opr);
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Map<String,BankCustomer> getCredentials() throws ClassNotFoundException, SQLException {
        Map<String,BankCustomer> map = new HashMap();
        conn = null;
        stmt = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM customer_info;" );
            while (rs.next()) {
                BankCustomer customer = new BankCustomer(rs.getString("userId"), rs.getString("firstName"),
                        rs.getString("lastName"), rs.getString("address"), rs.getString("password"));
                map.put(rs.getString("userID"), customer);
            }
        } finally {
            try {
                if ((conn != null && stmt != null) && (!conn.isClosed() && !stmt.isClosed())) {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private Map<String,BankAccount> getAccounts() throws ClassNotFoundException, SQLException {
        Map<String,BankAccount> map = new HashMap();
        conn = null;
        stmt = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM account_info;" );
            while (rs.next()) {
                BankAccount account = new BankAccount(rs.getString("accountID"), rs.getString("accountOwner"), rs.getDouble("balanceValue"));
                map.put(rs.getString("accountID"), account);
            }
        } finally {
            try {
                if ((conn != null && stmt != null) && (!conn.isClosed() && !stmt.isClosed())) {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private TreeSet<OperationRecord> getOperationRecords() throws ClassNotFoundException, SQLException {
        TreeSet<OperationRecord> oprs = new TreeSet();
        conn = null;
        stmt = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM operations;" );
            while (rs.next()) {
                OperationRecord opr = new OperationRecord(rs.getString("ID"), rs.getString("operationID"), rs.getString("operationType"),
                        rs.getString("accountID"), rs.getString("description"), rs.getString("transferredValue"));
                oprs.add(opr);
            }
        } finally {
            try {
                if ((conn != null && stmt != null) && (!conn.isClosed() && !stmt.isClosed())) {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return oprs;
    }

    public static int getSumOF(String DRIVER, String DB_URL, String columnName,String tableName) {
        conn = null;
        stmt = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            String sql = "SELECT SUM(" + columnName + ") " +
                    "FROM " + tableName + ";";
            ResultSet rs = stmt.executeQuery(sql);
            return rs.getInt(1);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if ((conn != null && stmt != null) && (!conn.isClosed() && !stmt.isClosed())) {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }
}
