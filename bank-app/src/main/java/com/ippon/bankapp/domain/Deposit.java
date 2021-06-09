package com.ippon.bankapp.domain;

public class Deposit {
    String id;
    String id2;
    String amount;

    public Deposit() {
    }

    public Deposit(String id, String amount) {
        this.id = id;
        this.amount = amount;
    }

    public Deposit(String id, String id2, String amount){
        this.id = id;
        this.id2 = id2;
        this.amount = amount;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
       String string = "Deposit{" +
                "id='" + id + '\'';
       if(!id2.equals("")){
           string += ", id2='" + id + '\'';
       }
       string += ", amount='" + amount + '\'' + '}';

       return string;
    }
}
