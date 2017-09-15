package com.mad.bank.common;

import java.io.Serializable;

public enum TransactionType implements Serializable {
    WITHDRAW,// (-1),
    DEPOSIT,// (1),
    TRANSFER// (0);
/*
    private final int multiplicator;
    TransactionType(int i) {
        this.multiplicator = i;
    }
    public int getMultiplicator() { return multiplicator; }
*/
}
