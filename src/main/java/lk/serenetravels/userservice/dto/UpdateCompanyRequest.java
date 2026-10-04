package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateCompanyRequest {

    @Schema(example = "Ceylon Holidays (Pvt) Ltd")
    @NotBlank
    private String name;

    @Schema(example = "42 Galle Road, Colombo 03")
    private String address;

    @Schema(example = "PV 00123456", description = "Business Registration (BR) number")
    private String registrationNumber;

    @Schema(example = "+94 11 234 5678")
    private String phone;

    @Schema(example = "info@ceylonholidays.lk")
    @Email
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
