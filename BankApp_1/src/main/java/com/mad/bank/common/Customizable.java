package com.mad.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Customizable extends Remote {
    public String getUserId() throws RemoteException;
    public String getFirstName() throws RemoteException;
    public String getLastName() throws RemoteException;
    public String getAddress() throws RemoteException;
    public Map<String, Acc> getAccountsMap() throws RemoteException;
}
