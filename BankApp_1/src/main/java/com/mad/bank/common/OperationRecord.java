package com.mad.bank.common;

import java.io.Serializable;

public class OperationRecord implements Serializable, Comparable {
    private String ID;
    private String operationID;
    private String type;
    private String accountID;
    private String title;
    private String value;

    public OperationRecord(String ID, String operationID, String type, String accountID, String title, String value) {
        this.ID = ID;
        this.operationID = operationID;
        this.type = type;
        this.accountID = accountID;
        this.title = title;
        this.value = value;
    }

    public String getID() { return ID; }
    public String getOperationID() { return operationID; }
    public String getType() { return this.type; }
    public String getAccountID() { return accountID; }
    public String getTitle() { return this.title; }
    public String getValue() { return this.value; }

    @Override
    public String toString() { return "------------------------------------------------\n"
            + "\tTitle: " + title + "\n\tOperation ID: " + operationID + "\n\tType: " + type + "\n\tAccount ID: " + accountID + "\n\tValue: " + value;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof OperationRecord) {
            String operID = ((OperationRecord) o).getID();
            int thisID = Integer.parseInt(ID);
            int thatID = Integer.parseInt(operID);
            if (thisID > thatID)
                return 1;
            else if (thisID == thatID)
                return 0;
            else
                return -1;
        }
        return 0;
    }
}