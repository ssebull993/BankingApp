package com.mad.bank.server;

import com.mad.bank.common.Communicative;
import com.mad.bank.common.Operation;
import com.mad.bank.common.TransactionType;
import com.mad.bank.server.UserClasses.BankAccount;
import com.mad.bank.server.UserClasses.BankCustomer;

import javax.xml.transform.Result;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public class DBEditor {
    private static int operationID = 1;

    public synchronized boolean withdraw(Statement stmt, Communicative client, Operation opr, Savepoint savepoint) throws SQLException {
        try {
            String accID = opr.getAccountFrom();
            String value = opr.getValue();
            String search = "SELECT * FROM account_info WHERE accountID == '" + accID +"';";
            ResultSet r = stmt.executeQuery(search);
            String balanceValue = r.getString("balanceValue");
            if (Double.parseDouble(balanceValue) >= Double.parseDouble(value)) {
                double updated = Double.parseDouble(balanceValue) - Double.parseDouble(value);
                String withdraw = "UPDATE account_info set balanceValue = '" + updated + "' WHERE accountID == " + accID + ";";
                stmt.executeUpdate(withdraw  + createRecord(opr, TransactionType.WITHDRAW));
                return true;
            } else if (Double.parseDouble(balanceValue) < Double.parseDouble(value)) {
                try {
                    client.messageClient("Not enough funds.", true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            stmt.getConnection().rollback(savepoint);
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean deposit(Statement stmt, Communicative client, Operation opr, Savepoint savepoint) throws SQLException {
        try {
            String accID = opr.getAccountTo();
            String value = opr.getValue();
            String search = "SELECT * FROM account_info WHERE accountID == '" + accID + "';";
            ResultSet r = stmt.executeQuery(search);
            String balanceValue = r.getString("balanceValue");
            double updated = Double.parseDouble(balanceValue) + Double.parseDouble(value);
            String deposit = "UPDATE account_info set balanceValue = '" + updated + "' WHERE accountID == '" + accID + "';";
            stmt.executeUpdate(deposit + createRecord(opr, TransactionType.DEPOSIT));
            return true;
        } catch (SQLException e) {
            stmt.getConnection().rollback(savepoint);
            e.printStackTrace();
            try {
                client.messageClient("Incorrect Account id!", true);
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
        return false;
    }

    public synchronized void transfer(Statement stmt, Communicative client, Operation opr, Savepoint savepoint) {
        try {
            String accIDFrom = opr.getAccountFrom();
            String value = opr.getValue();
            String search = "SELECT * FROM account_info WHERE accountID == '" + accIDFrom + "';";
            ResultSet r = stmt.executeQuery(search);
            String balanceValue = r.getString("balanceValue");
            if (Double.parseDouble(balanceValue) >= Double.parseDouble(value)) {
                double updated = Double.parseDouble(balanceValue) - Double.parseDouble(value);
                String withdraw = "UPDATE account_info set balanceValue = '" + updated + "' WHERE accountID == " + accIDFrom + ";";
                stmt.executeUpdate(withdraw + createRecord(opr, TransactionType.WITHDRAW));
            } else if (Double.parseDouble(balanceValue) < Double.parseDouble(value)) {
                try {
                    client.messageClient("Not enough funds.", true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            String accIDTo = opr.getAccountTo();
            search = "SELECT * FROM account_info WHERE accountID == '" + accIDTo + "';";
            ResultSet rs = stmt.executeQuery(search);
            balanceValue = rs.getString("balanceValue");
            rs.close();
            double updated = Double.parseDouble(balanceValue) + Double.parseDouble(value);
            String deposit = "UPDATE account_info set balanceValue = '" + updated + "' WHERE accountID == '" + accIDTo + "';";
            stmt.executeUpdate(deposit + createRecord(opr, TransactionType.DEPOSIT));
        } catch(SQLException e){
            try {
                stmt.getConnection().rollback(savepoint);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            try {
                client.messageClient("Incorrect Account id!", true);
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
    }

    private String createRecord(Operation opr, TransactionType tT) {
        String record = "";
        TransactionType transactionType = opr.getType();
        String description = "";
        String accountID = "";
        String transferredValue = opr.getValue();
        switch (tT) {
            case WITHDRAW:
                description = opr.getReceiver() + "\t" + opr.getTitle();
                accountID = opr.getAccountFrom();
                record = "','" + transactionType.toString() + "','" + description + "','" + accountID + "','" + "-" + transferredValue;
                break;
            case DEPOSIT:
                description = opr.getSender() + "\t" + opr.getTitle();
                accountID = opr.getAccountTo();
                record = "','" + transactionType.toString() + "','" + description + "','" + accountID + "','" + transferredValue;
                break;
        }
        String recordSQL = "\nINSERT INTO operations(" +
                "operationID, operationType, description, accountID, transferredValue" +
                ") VALUES ('" + operationID + record + "');";
        return recordSQL;
    }

    public static void getLastOperationId(String driver, String url) {
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            int index = 1;
            try {
                conn.setAutoCommit(false);
                ResultSet rs = stmt.executeQuery("SELECT operationID FROM operations;");
                while (rs.next()) {
                    index = rs.getRow();
                }
                index++;
                operationID = index;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if ((conn != null && stmt != null) || (!conn.isClosed() && !stmt.isClosed())) {
                        stmt.close();
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void createNewAccount(String DRIVER, String DB_URL, String accountOwner) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        Statement stmt = null;
        Savepoint savepoint = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            savepoint = conn.setSavepoint();
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            String sql = "INSERT INTO account_info(accountOwner, accountType) " +
                    "VALUES ('" + accountOwner + "','PERSONAL');";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(savepoint);
            e.printStackTrace();
        } finally {
            if ((conn != null && stmt != null) || (!conn.isClosed() && !stmt.isClosed())) {
                stmt.close();
                conn.close();
            }
        }
    }

    public synchronized void removeAccount(String DRIVER, String DB_URL, String accountID) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        Savepoint savepoint = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            savepoint = conn.setSavepoint();
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM account_info WHERE accountID == '" + accountID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            conn.rollback(savepoint);
        } finally {
            if ((conn != null && stmt != null) || (!conn.isClosed() && !stmt.isClosed())) {
                stmt.close();
                conn.close();
            }
        }
    }

    public synchronized void updateOperationID() {
        this.operationID++;
    }
}