package com.sty.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 使用 @ElementCollection 来存储多个姓名和电话号码
    @ElementCollection
    @CollectionTable(name = "contact_names", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "name")
    private List<String> names = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "contact_phones", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    private List<String> phoneNumbers = new ArrayList<>();

    private String email;
    private String location;
    private String info;
    private boolean favorite;
    private String mediaHandles;

    // 构造函数
    public Contact() {}

    public Contact(List<String> names, List<String> phoneNumbers, String location,
                  String email, boolean favorite, String mediaHandles) {
        this.names = names;
        this.phoneNumbers = phoneNumbers;
        this.location = location;
        this.email = email;
        this.favorite = favorite;
        this.mediaHandles = mediaHandles;
    }

    // ID getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Names getters/setters
    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    // 添加单个名字的便捷方法
    public void addName(String name) {
        if (this.names == null) {
            this.names = new ArrayList<>();
        }
        this.names.add(name);
    }

    // PhoneNumbers getters/setters
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    // 添加单个电话号码的便捷方法
    public void addPhoneNumber(String phoneNumber) {
        if (this.phoneNumbers == null) {
            this.phoneNumbers = new ArrayList<>();
        }
        this.phoneNumbers.add(phoneNumber);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getMediaHandles() {
        return mediaHandles;
    }

    public void setMediaHandles(String mediaHandles) {
        this.mediaHandles = mediaHandles;
    }
}