package com.restaurant.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tableId;
    private String time; // HH:MM
    private int guestsCount;
    private String userId;

    public Reservation() {}

    public Reservation(Long tableId, String time, int guestsCount, String userId) {
        this.tableId = tableId;
        this.time = time;
        this.guestsCount = guestsCount;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public int getGuestsCount() { return guestsCount; }
    public void setGuestsCount(int guestsCount) { this.guestsCount = guestsCount; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
