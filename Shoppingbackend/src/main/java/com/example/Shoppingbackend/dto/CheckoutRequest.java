package com.example.Shoppingbackend.dto;

public class CheckoutRequest {

    private String name;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String name, String phone, String address, String city, String state, String pincode) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isBlank()) sb.append(name).append(", ");
        if (phone != null && !phone.isBlank()) sb.append("Phone: ").append(phone).append(", ");
        if (address != null && !address.isBlank()) sb.append(address).append(", ");
        if (city != null && !city.isBlank()) sb.append(city).append(", ");
        if (state != null && !state.isBlank()) sb.append(state).append(" - ");
        if (pincode != null && !pincode.isBlank()) sb.append(pincode);
        return sb.toString().trim().replaceAll(", $", "");
    }
}
