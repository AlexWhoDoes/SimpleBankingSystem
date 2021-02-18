package banking;

import java.sql.*;
import java.util.Scanner;

public class BankingSystem {
    private final Scanner sc;
    String url;

    BankingSystem(String args) {
        this.sc = new Scanner(System.in);
        this.url = "jdbc:sqlite:" + args;
        createDataBase(url);
    }

    private void createDataBase(String url) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                            "id INTEGER PRIMARY KEY," +
                            "number TEXT NOT NULL," +
                            "pin TEXT NOT NULL," +
                            "balance INTEGER DEFAULT 0);");

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("something went wrong in second try with resources Banking System Class ");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        while (true) {
            System.out.println("\n1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");

            switch (sc.nextLine()) {
                case "1":
                    createAccount();
                    break;
                case "2":
                    logIntoAccount();
                    break;
                case "0":
                    System.out.println("\nBye!");
                    System.exit(0);
            }
        }
    }

    public void createAccount() {
        Account account = new Account();


        System.out.println("\nYour card has been created");
        System.out.println("Your card number:");
        System.out.println(account.getCard().getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(account.getCard().getPinCode());

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("INSERT INTO card (number, pin) VALUES (" +
                        account.getCard().getCardNumber() + ", " +
                        account.getCard().getPinCode() + ");");
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void logIntoAccount() {
        System.out.println("\nEnter your card number:");
        String cardNumber = sc.nextLine();
        System.out.println("\nEnter your PIN:");
        String pinCode = sc.nextLine();

        int id = -1;
        String numberDB;
        String pinDB;
        int balance = 0;
        boolean isLoggedIn = false;


        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet card = statement.executeQuery("SELECT * FROM card")) {
                    while (card.next()) {
                        id = card.getInt("id");
                        numberDB = card.getString("number");
                        pinDB = card.getString("pin");
                        balance = card.getInt("balance");
                        if (numberDB.equals(cardNumber)) {
                            if (pinCode.equals(pinDB)) {
                                isLoggedIn = true;
                                break;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isLoggedIn) {
            System.out.println("\nYou have successfully logged in!");
            while (true) {
                System.out.println("1. Balance\n" +
                        "2. Add income\n" +
                        "3. Do transfer\n" +
                        "4. Close account\n" +
                        "5. Log out\n" +
                        "0. Exit");

                switch (sc.next()) {
                    case "1":
                        if (id == -1) {
                            System.out.println("what a fuck with balance in switch");
                        } else {
                            printBalance(id);
                        }
                        break;
                    case "2":
                        if (id == -1) {
                            System.out.println("what a fuck in switch case loggedIn");
                        } else {
                            addIncome(id);
                        }
                        break;
                    case "3":
                        if (id == -1) {
                            System.out.println("what a fuck in switch case transfer");
                        } else {
                            doTransfer(id, cardNumber);
                        }
                        break;
                    case "4":
                        if (id == -1) {
                            System.out.println("what a fuck in delete is going on");
                        } else {
                            deleteAccount(id);
                        }
                        return;
                    case "5":
                        System.out.println("You have successfully logged out");
                        return;
                    case "0":
                        System.out.println("Bye!");
                        System.exit(0);
                }
            }
        } else {
            System.out.println("Wrong card number or PIN! ");
            run();
        }
    }

    private void doTransfer(int id, String cardNumber) {
        System.out.println("\nTransfer\n" +
                "Enter card number:");
        String input1 = sc.nextLine();
        String input = sc.nextLine();
        if (cardNumber.equals(input)) {
            System.out.println("You can't transfer money to the same account!");
        } else {
            boolean isCorrect = getLuhnResult(input);
            if (isCorrect) {
                if (isPresent(input)) {
                    transferMoney(id, input);
                } else {
                    System.out.println("Such a card does not exist.");
                }
            } else {
                System.out.println("Probably you made mistake in the card number. Please try again!");
            }
        }
    }

    private void transferMoney(int id, String input) {
        System.out.println("Enter how much money you want to transfer:");
        int moneyTransfer = sc.nextInt();
        int balance = 0;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement statement = conn.createStatement()) {
                ResultSet balanceSet = statement.executeQuery("SELECT balance FROM card WHERE id = " + id);
                balance = balanceSet.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (moneyTransfer < balance) {
            submitMoney(id, moneyTransfer, input);
        }
        else {
            System.out.println("Not enough money!");
        }
    }

    private void submitMoney(int id, int moneyTransfer, String input) {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("UPDATE card SET balance = balance - "
                        + moneyTransfer + " WHERE id = " + id + " ;" );
                statement.executeUpdate("UPDATE card SET balance = balance + "
                        + moneyTransfer + " WHERE number = " + input + " ;" );
                conn.commit();
            } catch (SQLException e) {
                System.out.println("there ? ");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("or there");
            e.printStackTrace();
        }
    }

    private boolean isPresent(String input) {
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet card = statement.executeQuery("SELECT * FROM card")) {
                    while (card.next()) {
                        String numberDB = card.getString("number");
                        if(numberDB.equals(input)) {
                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteAccount(int id) {
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("DELETE FROM card WHERE id = " + id);
                System.out.println("The account has been closed!\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printBalance(int id) {
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement statement = conn.createStatement()) {
                ResultSet balanceSet = statement.executeQuery("SELECT balance FROM card WHERE id = " + id);
                int balance = balanceSet.getInt("balance");
                System.out.println("Balance: " + balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addIncome(int id) {
        System.out.println();
        System.out.println("Enter income: ");
        int incomeEntered = sc.nextInt();

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            String insert = "UPDATE card SET balance = balance + ? WHERE id = ?";

            try (PreparedStatement preparedStatement = conn.prepareStatement(insert)) {
                preparedStatement.setInt(1, incomeEntered);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Income was added!");
        System.out.println();
    }

    private boolean getLuhnResult(String s) {
        int x = 0;
        for (int i = 0; i < s.length() - 1; i++) {
            int in = Integer.parseInt(String.valueOf(s.charAt(i)));
            if (i % 2 == 0) {
                x += luhnFormula(in);
            }
            else {
                x += in;
            }
        }
        x = (10 - x % 10) % 10;
        return Integer.parseInt(String.valueOf(s.charAt(15))) == x;
    }

    private int luhnFormula(int parseInt) {
        int x = parseInt * 2;
        if (x > 9) {
            return x - 9;
        }
        else return x;
    }
}