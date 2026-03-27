package com.example.farmermarketapp;

public class Product {

    private String id;
    private String name;
    private String category;
    private String description;
    private double price;
    private int quantity;
    private String farmerId;

    public Product() {}

    public Product(String id, String name, String category,
                   String description, double price,
                   int quantity, String farmerId) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.price       = price;
        this.quantity    = quantity;
        this.farmerId    = farmerId;
    }

    // Getters
    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public int getQuantity()       { return quantity; }
    public String getFarmerId()    { return farmerId; }

    // Setters
    public void setId(String id)             { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setCategory(String c)        { this.category = c; }
    public void setDescription(String d)     { this.description = d; }
    public void setPrice(double price)       { this.price = price; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
}