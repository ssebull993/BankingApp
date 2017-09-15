package com.mad.bank.server.UserClasses;

import com.mad.bank.common.Acc;
import com.mad.bank.common.OperationRecord;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class BankAccount implements Serializable, Acc{
    private final String accountID;
    private final String userID;
    private double balance;
    private TreeSet<OperationRecord> oprs = new TreeSet<>();

    public BankAccount(String id, String userID, double value) {
        this.accountID = id;
        this.userID = userID;
        this.balance = value;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public String getBalance() {
        return Double.toString(this.balance);
    }

    @Override
    public TreeSet<OperationRecord> getOperations() throws RemoteException {
        return oprs;
    }

    public void addOperation(OperationRecord opr) {
        if (!oprs.contains(opr)) { oprs.add(opr); }
    }

    @Override
    public String toString() {
        return "Account ID: " + accountID + " | Owner ID: " + userID + " | Balance: " + balance;
    }
}
