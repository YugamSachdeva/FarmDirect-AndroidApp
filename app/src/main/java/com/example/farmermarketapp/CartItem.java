package com.example.farmermarketapp;

public class CartItem {

    private String docId;
    private String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String farmerId;

    public CartItem() {}

    // Getters
    public String getDocId()      { return docId; }
    public String getProductId()  { return productId; }
    public String getName()       { return name; }
    public String getCategory()   { return category; }
    public double getPrice()      { return price; }
    public int getQuantity()      { return quantity; }
    public String getFarmerId()   { return farmerId; }

    // Setters
    public void setDocId(String docId)         { this.docId = docId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name)           { this.name = name; }
    public void setCategory(String category)   { this.category = category; }
    public void setPrice(double price)         { this.price = price; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
    public void setFarmerId(String farmerId)   { this.farmerId = farmerId; }
}