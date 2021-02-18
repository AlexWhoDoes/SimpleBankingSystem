package banking;

public class Account {
    private final double balance;
    private final CreditCard card;

    Account() {
        this.balance = 0;
        this.card = new CreditCard();
    }

    public double getBalance() {
        return balance;
    }

    public CreditCard getCard() {
        return card;
    }
}