package com.sty.controller;

import com.sty.entity.Contact;
import com.sty.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin // 如果需要跨域支持
public class ContactController {

    @Autowired
    private ContactService contactService;

    // 获取所有联系人
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    // 根据ID获取联系人
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        if (contact != null) {
            return ResponseEntity.ok(contact);
        }
        return ResponseEntity.notFound().build();
    }

    // 创建新联系人
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        Contact createdContact = contactService.createContact(contact);
        return ResponseEntity.ok(createdContact);
    }

    // 更新联系人
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(id);
        contactService.updateContact(contact);
        return ResponseEntity.ok().build();
    }

    // 删除联系人
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok().build();
    }

    // 获取收藏的联系人
    @GetMapping("/favorites")
    public ResponseEntity<List<Contact>> getFavoriteContacts() {
        return ResponseEntity.ok(contactService.getFavoriteContacts());
    }

    // 更新收藏状态
    @PutMapping("/{id}/favorite")
    public ResponseEntity<Void> updateFavoriteStatus(
            @PathVariable Long id,
            @RequestParam boolean favorite) {
        contactService.updateFavoriteStatus(id, favorite);
        return ResponseEntity.ok().build();
    }

    // 搜索联系人
    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String keyword) {
        return ResponseEntity.ok(contactService.searchContacts(keyword));
    }

    // 导出联系人到Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        try {
            ByteArrayInputStream in = contactService.exportToExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=contacts.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 从Excel导入联系人
    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            contactService.importFromExcel(file);
            return ResponseEntity.ok("Import successful");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Import failed: " + e.getMessage());
        }
    }

    // 为联系人添加新的名字
    @PostMapping("/{id}/names")
    public ResponseEntity<Contact> addNameToContact(
            @PathVariable Long id,
            @RequestParam String name) {
        Contact contact = contactService.getContactById(id);
        if (contact != null) {
            contact.addName(name);
            contactService.updateContact(contact);
            return ResponseEntity.ok(contact);
        }
        return ResponseEntity.notFound().build();
    }

    // 为联系人添加新的电话号码
    @PostMapping("/{id}/phones")
    public ResponseEntity<Contact> addPhoneNumberToContact(
            @PathVariable Long id,
            @RequestParam String phoneNumber) {
        Contact contact = contactService.getContactById(id);
        if (contact != null) {
            contact.addPhoneNumber(phoneNumber);
            contactService.updateContact(contact);
            return ResponseEntity.ok(contact);
        }
        return ResponseEntity.notFound().build();
    }
}