package com.mad.bank.server;

import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.Scanner;

public class BankServer {
    public static void main(String[] args) {

        int initialTransactionSum = DBInitializer.getSumOF("org.sqlite.JDBC", "jdbc:sqlite:Bank.db",
                "transferredValue","operations");
        int initialBalanceSum = DBInitializer.getSumOF("org.sqlite.JDBC", "jdbc:sqlite:Bank.db",
                "balanceValue","account_info");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            int finalTransactionSum = DBInitializer.getSumOF("org.sqlite.JDBC", "jdbc:sqlite:Bank.db",
                    "transferredValue", "operations");
            int finalBalanceSum = DBInitializer.getSumOF("org.sqlite.JDBC", "jdbc:sqlite:Bank.db",
                    "balanceValue", "account_info");
            int fbs = finalBalanceSum - initialBalanceSum;
            int fts = finalTransactionSum - initialTransactionSum;
            System.out.println("------------------------------------------------------------------");
            System.out.println("Initial Total Balance: \t" + initialBalanceSum);
            System.out.println("Final Total Balance: \t" + finalBalanceSum);
            System.out.println("Balance Difference: \t" + fbs);
            System.out.println("------------------------------------------------------------------");
            System.out.println("Initial Transactions sum: \t" + initialTransactionSum);
            System.out.println("Final Transactions sum: \t" + finalTransactionSum);
            System.out.println("Transactions Difference: \t" + fts);
            System.out.println("------------------------------------------------------------------");
        }));
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            BankServant bankServant = new BankServant();
            registry.rebind("bankTest", bankServant);
            System.out.println("Server ready!");
            System.out.println("Type 'E' to exit.");
            while(true) {
                Scanner in = new Scanner(System.in);
                if (in.nextLine().equalsIgnoreCase("E")) {
                    System.exit(0);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
