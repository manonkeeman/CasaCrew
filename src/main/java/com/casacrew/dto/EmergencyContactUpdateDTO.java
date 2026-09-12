package com.casacrew.dto;

import jakarta.validation.constraints.Size;

public class EmergencyContactUpdateDTO {

    @Size(max = 30, message = "Telefoonnummer mag maximaal 30 tekens zijn")
    private String phoneNumber;

    public EmergencyContactUpdateDTO() {}

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
