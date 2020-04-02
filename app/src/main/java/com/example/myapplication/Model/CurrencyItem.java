package com.example.myapplication.Model;

public class CurrencyItem {

    private String description;
    private double amount;
    private String currencyName;
    private String countryCode;

    public CurrencyItem() {
    }

    public CurrencyItem(String description, double amount, String currencyName, String countryCode) {
        this.description = description;
        this.amount = amount;
        this.currencyName = currencyName;
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
