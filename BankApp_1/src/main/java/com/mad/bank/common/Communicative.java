package com.mad.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Communicative extends Remote {
    public void messageClient(String in, boolean isError) throws RemoteException;
    public void update() throws RemoteException;
}
