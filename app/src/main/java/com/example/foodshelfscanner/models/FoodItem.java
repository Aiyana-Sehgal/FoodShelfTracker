package com.example.foodshelfscanner.models;

public class FoodItem {
    private int id;
    private String name;
    private String type;
    private String brand;
    private String quantity;
    private String location;
    private String addedTime;
    private int shelfLifeDays;
    private int totalDays;
    private int imageResource;

    public FoodItem(int id, String name, String type, String brand, String quantity,
                    String location, String addedTime, int shelfLifeDays,
                    int totalDays, int imageResource) {
        this.id = id;
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
    public int getId() { return id; }
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
        if (totalDays == 0) return 0;
        return (int) ((float) shelfLifeDays / totalDays * 100);
    }
}
