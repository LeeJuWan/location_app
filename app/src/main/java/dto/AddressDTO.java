package dto;

public class AddressDTO {
    private String phone;
    private String name;

    public AddressDTO(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
