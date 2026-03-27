package com.example.farmermarketapp;

public class CustomerOrderModel {

    private String docId;
    private String name;
    private double price;
    private int quantity;
    private String status;
    private String farmerId;
    private long timestamp;

    public CustomerOrderModel() {}

    public String getDocId()    { return docId; }
    public String getName()     { return name; }
    public double getPrice()    { return price; }
    public int getQuantity()    { return quantity; }
    public String getStatus()   { return status; }
    public String getFarmerId() { return farmerId; }
    public long getTimestamp()  { return timestamp; }

    public void setDocId(String d)    { this.docId = d; }
    public void setName(String n)     { this.name = n; }
    public void setPrice(double p)    { this.price = p; }
    public void setQuantity(int q)    { this.quantity = q; }
    public void setStatus(String s)   { this.status = s; }
    public void setFarmerId(String f) { this.farmerId = f; }
    public void setTimestamp(long t)  { this.timestamp = t; }
}