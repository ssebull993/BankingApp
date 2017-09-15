package com.mad.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface Usable extends Remote {
    public boolean login(String id, String password, Communicative client) throws RemoteException;
    public void logout(Communicative client) throws RemoteException;
    public boolean makeOperation(Communicative client, Operation record) throws RemoteException;
    public Customizable getBankClient(Communicative client) throws RemoteException;
    public void createNewAccount(Communicative client) throws RemoteException;
    public void removeAccount(Communicative client, String accountID) throws RemoteException;
}
