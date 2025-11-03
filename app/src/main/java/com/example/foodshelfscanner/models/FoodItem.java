package com.example.foodshelfscanner.models;

public class FoodItem {
    private String name;
    private String type;
    private String brand;
    private String quantity;
    private String location;
    private String addedTime;
    private int shelfLifeDays;
    private int totalDays;
    private int imageResource;

    public FoodItem(String name, String type, String brand, String quantity,
                    String location, String addedTime, int shelfLifeDays,
                    int totalDays, int imageResource) {
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.quantity = quantity;
        this.location = location;
        this.addedTime = addedTime;
        this.shelfLifeDays = shelfLifeDays;
        this.totalDays = totalDays;
        this.imageResource = imageResource;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getBrand() { return brand; }
    public String getQuantity() { return quantity; }
    public String getLocation() { return location; }
    public String getAddedTime() { return addedTime; }
    public int getShelfLifeDays() { return shelfLifeDays; }
    public int getTotalDays() { return totalDays; }
    public int getImageResource() { return imageResource; }

    public int getProgressPercentage() {
        return (int) ((float) shelfLifeDays / totalDays * 100);
    }
}
