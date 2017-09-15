package com.mad.bank.client;

import com.mad.bank.common.Acc;
import com.mad.bank.common.Communicative;
import com.mad.bank.common.Customizable;
import com.mad.bank.common.Operation;
import com.mad.bank.common.OperationRecord;
import com.mad.bank.common.TransactionType;
import com.mad.bank.common.Usable;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class UserServant extends UnicastRemoteObject implements Runnable, Communicative {
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d{1,14}(\\.\\d{0,2})?");
    private static final Pattern INT_PATTERN = Pattern.compile("\\d{1,10}");
    private final Usable bankServer;
    private final String id;
    private Scanner in = new Scanner(System.in);
    private Customizable bankCustomer;
    private Map<String, Acc> bankAccounts = new HashMap<>();
    private transient boolean hasStopped;
    private volatile boolean done = false;

    protected UserServant(String id, Usable bankServer) throws RemoteException {
        this.id = id;
        this.bankServer = bankServer;
        this.hasStopped = false;
    }

    @Override
    public void messageClient(String in, boolean isError) throws RemoteException {
        if (isError)
            System.err.println(in);
        else
            System.out.println(in);
    }

    public boolean hasStopped() {
        return this.hasStopped;
    }

    private void stop() {
        this.done = true;
    }

    private void showAccInfo() {
        Set<Map.Entry<String, Acc>> entrySet = bankAccounts.entrySet();
        System.out.println("------------------------------------------------");
        for (Map.Entry<String, Acc> entry : entrySet) {
            System.out.println(entry.getValue());
        }
    }

    private boolean operation(TransactionType type) throws RemoteException {
        boolean exitFlag = false;
        String accID = "";
        String value = "";
        showAccInfo();
        System.out.println("------------------------------------------------");
        do {
            System.out.println("Type 'E' to cancel");
            System.out.println("Account ID: ");
            accID = in.nextLine();
            if (accID.equalsIgnoreCase("E")) {
                exitFlag = true;
            } else if (!bankAccounts.containsKey(accID)) {
                System.err.println("Wrong Acc ID!");
            }
        } while (!exitFlag && !bankAccounts.containsKey(accID));
        while (!exitFlag && !DECIMAL_PATTERN.matcher(value).matches()) {
            System.out.println("Type 'E' to cancel");
            System.out.println("Value : ");
            value = in.nextLine();
            if (value.equalsIgnoreCase("E")) {
                exitFlag = true;
            }
        }
        if(!exitFlag) {
            Operation opr = new Operation(
                    type, accID, bankCustomer.getFirstName(), bankCustomer.getLastName(), bankCustomer.getAddress(), value
            );
            boolean state = bankServer.makeOperation(this, opr);
            if (state) {
                System.out.println("Current Balance: " + bankAccounts.get(accID).getBalance());
                return true;
            }
        }
        return false;
    }

    private boolean operationRemove(TransactionType type, String accID) throws RemoteException {
        System.out.println("------------------------------------------------");
        if (!accID.equalsIgnoreCase("")){
            String value = bankAccounts.get(accID).getBalance();
            Operation opr = new Operation(
                    type, accID, bankCustomer.getFirstName(), bankCustomer.getLastName(), bankCustomer.getAddress(), value
            );
            boolean state = bankServer.makeOperation(this, opr);
            if (state) {
                System.out.println("Current Balance: " + bankAccounts.get(accID).getBalance());
                return true;
            }
        }
        return false;
    }

    private boolean transfer() throws RemoteException {
        boolean exitFlag = false;
        String accIDFrom = "";
        String accIDTo = "";
        String value = "";
        showAccInfo();
        System.out.println("------------------------------------------------");
        do {
            System.out.println("Type 'E' to cancel");
            System.out.println("Account Id of origin: ");
            accIDFrom = in.nextLine();
            if (accIDFrom.equalsIgnoreCase("E")) {
                exitFlag = true;
            } else if (!bankAccounts.containsKey(accIDFrom)) {
                System.err.println("Wrong Acc ID!");
            }
        } while (!exitFlag && !bankAccounts.containsKey(accIDFrom));

        while (!exitFlag && (!INT_PATTERN.matcher(accIDTo).matches() || accIDFrom.equalsIgnoreCase(accIDTo))) {
            System.out.println("Type 'E' to cancel");
            System.out.println("Account Id of Receiver: ");
            accIDTo = in.nextLine();
            if (accIDTo.equalsIgnoreCase("E")) {
                exitFlag = true;
            } else if (accIDFrom.equalsIgnoreCase(accIDTo) || !INT_PATTERN.matcher(accIDTo).matches()) {
                System.err.println("Wrong Acc ID!");
            }
        }

        while (!exitFlag && !DECIMAL_PATTERN.matcher(value).matches()) {
            System.out.println("Type 'E' to cancel");
            System.out.println("Value : ");
            value = in.nextLine();
            if (value.equalsIgnoreCase("E")) {
                exitFlag = true;
            }
        }
        if(!exitFlag) {
            System.out.println("Receiver's: ");
            System.out.println("-First name: ");
            String firstName = in.nextLine();
            System.out.println("-Last name: ");
            String lastName = in.nextLine();
            System.out.println("-Address: ");
            String address = in.nextLine();
            System.out.println("-Title : ");
            String title = in.nextLine();
            Operation opr = new Operation(
                    accIDFrom, bankCustomer.getFirstName(), bankCustomer.getLastName(), bankCustomer.getAddress(),
                    accIDTo, firstName, lastName, address,
                    title, value
            );
            boolean state = bankServer.makeOperation(this, opr);
            if (state) {
                System.out.println("Current Balance: " + bankAccounts.get(accIDFrom).getBalance());
                return true;
            }
        }
        return false;
    }

    private void getOperations() throws RemoteException {
        showAccInfo();
        System.out.println("------------------------------------------------");
        System.out.println("Chose account: ");
        String accID = in.nextLine();
        if (bankAccounts.containsKey(accID)) {
            Set<OperationRecord> oprs = bankAccounts.get(accID).getOperations();
            if (!oprs.isEmpty()) {
                for (OperationRecord opr : oprs) {
                    System.out.println(opr.toString());
                }
            } else {
                System.out.println("No records found.");
            }
        }
    }

    private void removeAccount() throws RemoteException {
        boolean exitFlag = false;
        boolean boo = false;
        String accID = "";
        showAccInfo();
        System.out.println("------------------------------------------------");
        do {
            System.out.println("Type 'E' to cancel");
            System.out.println("Account ID to remove: ");
            accID = in.nextLine();
            if (accID.equalsIgnoreCase("E")) {
                exitFlag = true;
            } else if (!bankAccounts.containsKey(accID)) {
                System.err.println("Wrong Acc ID!");
            }
        } while (!exitFlag && !bankAccounts.containsKey(accID));

        if (!exitFlag && Double.parseDouble(bankAccounts.get(accID).getBalance()) > 0.0) {
            boo = operationRemove(TransactionType.WITHDRAW,accID);
        }
        if (!exitFlag && (Double.parseDouble(bankAccounts.get(accID).getBalance()) == 0.0 || boo))
            bankServer.removeAccount(this, accID);
    }

    private void addAccount() throws RemoteException {
        System.out.println("------------------------------------------------");
        String accID = in.nextLine();
        Set<OperationRecord> oprs = bankAccounts.get(accID).getOperations();
        if (!oprs.isEmpty()) {
            for (OperationRecord opr : oprs) {
                System.out.println(opr.toString());
            }
        } else {
            System.out.println("No records found.");
        }
    }

    @Override
    public void update() {
        try {
            this.bankCustomer = bankServer.getBankClient(this);
            this.bankAccounts = bankCustomer.getAccountsMap();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bankServer.logout(this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }));
        update();
        while(!done) {
            System.out.println(
                    "------------------------------------------------\n" +
                    "Chose option:\n\t" +
                        "[1] Show Accounts and their balances.\n\t" +
                        "[2] Deposit.\n\t" +
                        "[3] Withdraw.\n\t" +
                        "[4] Transfer.\n\t" +
                        "[5] Customer info.\n\t" +
                        "[6] Show operations for given account.\n\t" +
                        "---------------------------------------\n\t" +
                        "[7] Remove Account\n\t" +
                        "[8] Add Account\n\t" +
                        "---------------------------------------\n\t" +
                        "[E] Exit."
            );
            try {
                String input = in.nextLine();
                switch (input.toUpperCase()) {
                    case "1":
                        showAccInfo();
                        break;
                    case "2":
                        operation(TransactionType.DEPOSIT);
                        break;
                    case "3":
                        operation(TransactionType.WITHDRAW);
                        break;
                    case "4":
                        transfer();
                        break;
                    case "5":
                        System.out.println("------------------------------------------------");
                        System.out.println(bankCustomer.getFirstName() + " " + bankCustomer.getLastName() + "\n" + bankCustomer.getAddress());
                        break;
                    case "6":
                        getOperations();
                        break;
                    case "7":
                        if (bankAccounts.size() > 1) {
                            removeAccount();
                        } else {
                            System.out.println("Not enough Accounts!");
                        }
                        break;
                    case "8":
                        bankServer.createNewAccount(this);
                        break;
                    case "E":
                        stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                try {
                    //bankServer.logout(this);
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}