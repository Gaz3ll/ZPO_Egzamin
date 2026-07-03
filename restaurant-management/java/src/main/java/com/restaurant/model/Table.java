package com.restaurant.model;

import jakarta.persistence.*;

@Entity
@jakarta.persistence.Table(name = "tables")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int seats;
    private String location; // INDOOR or OUTDOOR

    public RestaurantTable() {}

    public RestaurantTable(int seats, String location) {
        this.seats = seats;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
