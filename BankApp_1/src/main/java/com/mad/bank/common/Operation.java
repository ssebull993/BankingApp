package com.mad.bank.common;

import java.io.Serializable;

public class Operation implements Serializable {
	private TransactionType type;
	private String accountNoFrom;
	private String sender;
	private String accountNoTo;
	private String receiver;
	private String title;
	private String value;

    public Operation(TransactionType type, String accountNo, String firstName, String lastName,
                     String address, String value) {
        this.type = type;
        this.value = value;
        this.title = "";
        switch (type) {
            case WITHDRAW:
                this.accountNoFrom = accountNo;
                this.sender = firstName + " " + lastName + " " + address + " ";
                this.accountNoTo = "0";
                this.receiver = "ATM"; // Automated Teller Machine
                break;
            case DEPOSIT:
                this.accountNoFrom = "0";
                this.sender = "CDM"; // Cash Deposit Machine
                this.accountNoTo = accountNo;
                this.receiver = firstName + " " + lastName + " " + address + " ";
                break;
        }
    }

	public Operation(String accountNoFrom, String firstNameFrom, String lastNameFrom, String addressFrom, String accountNoTo,
                     String firstNameTo, String lastNameTo, String addressTo, String title, String value) {
		this.type = TransactionType.TRANSFER;
		this.accountNoFrom = accountNoFrom;
		this.sender = firstNameFrom + " " + lastNameFrom + "\n\t" + addressFrom + "\t" + accountNoFrom;
		this.accountNoTo = accountNoTo;
		this.receiver = firstNameTo + " " + lastNameTo + " " + addressTo + "\t" + accountNoTo;
		this.title = title;
		this.value = value;
	}
	public TransactionType getType() { return this.type; }
	public String getAccountFrom() { return this.accountNoFrom; }
	public String getSender() { return this.sender; }
	public String getAccountTo() { return this.accountNoTo; }
    public String getReceiver() { return this.receiver; }
	public String getTitle() { return this.title; }
	public String getValue() { return this.value; }
}
