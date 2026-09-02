package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWorkspaceRequest {

    @NotBlank(message = "Çalışma alanı adı boş olamaz.")
    @Size(min = 2, max = 100, message = "Çalışma alanı adı 2 ile 100 karakter arasında olmalıdır.")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    public CreateWorkspaceRequest() {}

    public CreateWorkspaceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
