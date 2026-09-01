package com.casacrew.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "waste_schedule_entries")
public class WasteScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "waste_type", nullable = false, length = 100)
    private String wasteType;

    @Size(max = 255, message = "Schema mag maximaal 255 tekens zijn")
    @Column(name = "schedule_info", length = 255)
    private String scheduleInfo;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    public WasteScheduleEntry() {
    }

    public WasteScheduleEntry(String wasteType, String scheduleInfo, int orderIndex) {
        this.wasteType = wasteType;
        this.scheduleInfo = scheduleInfo;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }
    public String getScheduleInfo() { return scheduleInfo; }
    public void setScheduleInfo(String scheduleInfo) { this.scheduleInfo = scheduleInfo; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
}
