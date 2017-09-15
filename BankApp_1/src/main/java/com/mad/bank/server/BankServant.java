package com.mad.bank.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mad.bank.common.Communicative;
import com.mad.bank.common.Customizable;
import com.mad.bank.common.Operation;
import com.mad.bank.common.Usable;
import com.mad.bank.server.UserClasses.BankCustomer;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class BankServant extends UnicastRemoteObject implements Usable {
    private final String DRIVER = "org.sqlite.JDBC";
    private final String DB_URL = "jdbc:sqlite:Bank.db";
    private DBInitializer dbInitializer = new DBInitializer(DRIVER, DB_URL);
    private BiMap<Communicative, BankCustomer> loggedInUsers = HashBiMap.create();
    private Map<String, BankCustomer> customers = new HashMap<>();
    private DBEditor editor = new DBEditor();

    public BankServant() throws RemoteException, ClassNotFoundException, SQLException {
        this.customers = dbInitializer.getCustomers();
        DBEditor.getLastOperationId(DRIVER, DB_URL);
    }

    @Override
    public boolean login(String userId, String password, Communicative client) throws RemoteException {
        if(!loggedInUsers.containsKey(client)
                && customers.containsKey(userId) && password.equals(customers.get(userId).getPassword())) {
            try {
                loggedInUsers.put(client, customers.get(userId));
            } catch (IllegalArgumentException e) {
                client.messageClient("This User is already logged in.", true);
                return false;
            }
            System.out.println(loggedInUsers);
            return true;
        }else{
            client.messageClient("Wrong User ID or password.", true);
            return false;
        }
    }

    @Override
    public void logout(Communicative client) throws RemoteException {
        if (loggedInUsers.containsKey(client)) {
            loggedInUsers.remove(client);
            System.out.println(loggedInUsers);
        } else {
            System.out.println("Logging out failed...");
        }
    }

    @Override
    public boolean makeOperation(Communicative client, Operation record){
        Connection conn = null;
        Statement stmt = null;
        Savepoint savepoint = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            savepoint = conn.setSavepoint();
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            switch (record.getType()) {
                case DEPOSIT:
                    synchronized (editor) {
                        editor.deposit(stmt, client, record);
                        editor.updateOperationID();
                    }
                    break;
                case WITHDRAW:
                    synchronized (editor) {
                        editor.withdraw(stmt, client, record);
                        editor.updateOperationID();
                    }
                    break;
                case TRANSFER:
                    synchronized (editor) {
                        editor.transfer(stmt, client, record);
                        editor.updateOperationID();
                    }
                    break;
            }
            stmt.close();
            conn.commit();
            conn.close();
            updateAll();
            return true;
        } catch (RemoteException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && stmt != null) {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public Customizable getBankClient(Communicative client) throws RemoteException {
        return loggedInUsers.get(client);
    }

    private void updateAll() throws SQLException, ClassNotFoundException, RemoteException {
        dbInitializer.assignAccounts();
        dbInitializer.assignOperations();
        Set<Map.Entry<Communicative, BankCustomer>> entrySet = loggedInUsers.entrySet();
        for (Map.Entry<Communicative, BankCustomer> entry : entrySet) {
            try {
                entry.getKey().update();
            } catch (ConnectException e) {
                System.out.println(entry.getValue().getUserId());
                logout(entry.getKey());
            }
        }
    }

    @Override
    public void createNewAccount(Communicative client) throws RemoteException {
        try {
            editor.createNewAccount(DRIVER, DB_URL, loggedInUsers.get(client).getUserId());
            dbInitializer.assignAccounts();
            dbInitializer.assignOperations();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            client.messageClient("No such account ID", true);
        }
        client.update();
    }

    @Override
    public void removeAccount(Communicative client, String accountID) throws RemoteException {
        try {
            editor.removeAccount(DRIVER, DB_URL, accountID);
            dbInitializer.assignAccounts();
            dbInitializer.assignOperations();
            loggedInUsers.get(client).removeAccount(accountID);
            client.messageClient("Done",false);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            client.messageClient("No such account ID", true);
        }
        client.update();
    }
}
