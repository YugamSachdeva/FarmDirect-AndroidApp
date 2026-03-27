package com.example.farmermarketapp;

public class FarmerOrderModel {

    private String docId;
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private String status;
    private String customerId;
    private long timestamp;
    private String customerName;
    private String customerCity;
    private String customerPhone;

    public FarmerOrderModel() {}

    public String getDocId()        { return docId; }
    public String getProductId()    { return productId; }
    public String getName()         { return name; }
    public double getPrice()        { return price; }
    public int getQuantity()        { return quantity; }
    public String getStatus()       { return status; }
    public String getCustomerId()   { return customerId; }
    public long getTimestamp()      { return timestamp; }
    public String getCustomerName() { return customerName; }
    public String getCustomerCity() { return customerCity; }
    public String getCustomerPhone(){ return customerPhone; }

    public void setDocId(String d)          { this.docId = d; }
    public void setProductId(String p)      { this.productId = p; }
    public void setName(String n)           { this.name = n; }
    public void setPrice(double p)          { this.price = p; }
    public void setQuantity(int q)          { this.quantity = q; }
    public void setStatus(String s)         { this.status = s; }
    public void setCustomerId(String c)     { this.customerId = c; }
    public void setTimestamp(long t)        { this.timestamp = t; }
    public void setCustomerName(String n)   { this.customerName = n; }
    public void setCustomerCity(String c)   { this.customerCity = c; }
    public void setCustomerPhone(String p)  { this.customerPhone = p; }
}