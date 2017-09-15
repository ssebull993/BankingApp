package com.mad.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Acc extends Remote {
    public Set<OperationRecord> getOperations() throws RemoteException;
    public String getBalance() throws RemoteException;
}
