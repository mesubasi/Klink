package com.urlshortener.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Platform admin request to open a customer workspace and appoint its manager. */
public class CreateCustomerRequest {

    @NotBlank(message = "Şirket adı boş olamaz.")
    @Size(max = 100, message = "Şirket adı en fazla 100 karakter olabilir.")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    @NotBlank(message = "Yönetici e-posta adresi boş olamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String managerEmail;

    /** null = platform default, 0 = unlimited. */
    private Integer maxMembers;
    private Integer maxLinks;

    public CreateCustomerRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getManagerEmail() { return managerEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public Integer getMaxLinks() { return maxLinks; }
    public void setMaxLinks(Integer maxLinks) { this.maxLinks = maxLinks; }
}
