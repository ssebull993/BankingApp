package com.mad.bank.client;

import com.mad.bank.common.Usable;
import com.mad.bank.server.BankServer;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UserClient {
    public static void main(String[] args) {
        UserServant userServant;
        String id = "";
        String password = "";
        try {
            Usable service = (Usable) Naming.lookup("rmi://localhost:1099/bankTest"); //192.168.0.178
            Scanner in = new Scanner(System.in);
            boolean state = false;
            while(!state) {
                if (args.length == 2) {
                    id = args[0];
                    password = args[1];
                } else {
                    System.out.println("User ID: ");
                    id = in.nextLine();
                    System.out.println("Password: ");
                    password = in.nextLine();
                }
                userServant = new UserServant(id, service);
                if (service.login(id, password, userServant)) {
                    Thread thread = new Thread(userServant);
                    thread.start();
                    thread.join();
                }
                TimeUnit.MILLISECONDS.sleep(50);
                System.out.println("Do you want to try again? (Y/N)");
                String option = in.nextLine();
                if (!option.equalsIgnoreCase("Y"))
                    state = true;
            }
        } catch(NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (ConnectException e) {
            System.err.println("Server Not available... Try again later.");
        } catch (RemoteException | NotBoundException | MalformedURLException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
