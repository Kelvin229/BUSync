package com.cosc3p97project.busync.model;

public class Contacts {

    // Contact Variables.
    private String name;
    private String image;
    private String status;
    private String request_type;

    // default constructor
    public Contacts() {}

    // overload constructor with contact items such as names, images, status, request_type.
    public Contacts(String name, String image, String status, String request_type) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.request_type = request_type;
    }

    // getters and setters.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestType() {
        return request_type;
    }

    public void setRequestType(String request_type) {
        this.request_type = request_type;
    }
}
