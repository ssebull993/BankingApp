package com.mad.bank.server.UserClasses;

import com.mad.bank.common.Acc;
import com.mad.bank.common.Customizable;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class BankCustomer implements Serializable, Customizable {
        private final String userId;
    private String firstName;
    private String lastName;
    private String address;
    private transient String password;

    private Map<String, BankAccount> bankAccountsMap = new HashMap<>();

    public BankCustomer(String userId, String firstName, String lastName, String address, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
    }
    @Override
    public String getUserId() { return userId; }
    @Override
    public String getFirstName() { return this.firstName; }
    @Override
    public String getLastName() { return this.lastName; }
    @Override
    public String getAddress() { return this.address; }

    @Override
    public Map<String, Acc> getAccountsMap() throws RemoteException {
        Map<String,Acc> accMap = new HashMap<>();
        accMap.putAll(this.bankAccountsMap);
        return accMap;
    }

    public String getPassword() { return this.password; }

    public void putAccount(BankAccount account) {
        String id = account.getAccountID();
        if (bankAccountsMap.containsKey(id)) {
            bankAccountsMap.replace(id, account);
        } else {
            bankAccountsMap.put(id, account);
        }
    }

    public void removeAccount(String accID) {
        bankAccountsMap.remove(accID, bankAccountsMap.get(accID));
    }
}