package banking;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CreditCard {
    private String cardNumber;
    private String pinCode;

    CreditCard() {
        createCardNumber();
        createPinCode();
    }

    private void createCardNumber() {
        String number = "400000" + getAccountIdentifier();
        this.cardNumber =  number + getCheckSum(number);
    }

    private void createPinCode() {
        this.pinCode = String.valueOf(new Random().nextInt(9000) + 1000);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPinCode() {
        return pinCode;
    }

    private int getCheckSum(String s) {
        int x = getLuhnResult(s);
        return (10 - x % 10) % 10;
    }

    private long getAccountIdentifier() {
        return ThreadLocalRandom.current().nextLong(100_000_000, 1_000_000_000);
    }

    private int getLuhnResult(String s) {
        int x = 0;
        for (int i = 0; i < s.length(); i++) {
            int in = Integer.parseInt(String.valueOf(s.charAt(i)));
            if (i % 2 == 0) {
                x += luhnFormula(in);
            }
            else {
                x += in;
            }
        }
        return x;
    }

    private int luhnFormula(int parseInt) {
        int x = parseInt * 2;
        if (x > 9) {
            return x - 9;
        }
        else return x;
    }

}