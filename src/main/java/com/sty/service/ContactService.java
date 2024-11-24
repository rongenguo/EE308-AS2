package com.sty.service;

import com.sty.mapper.ContactMapper;
import com.sty.entity.Contact;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactMapper contactMapper;

    // 获取所有联系人
    public List<Contact> getAllContacts() {
        List<Contact> contacts = contactMapper.findAll();
        // 为每个联系人加载其名字和电话号码
        for (Contact contact : contacts) {
            loadContactDetails(contact);
        }
        return contacts;
    }

    // 根据ID获取联系人
    public Contact getContactById(Long id) {
        Contact contact = contactMapper.findById(id);
        if (contact != null) {
            loadContactDetails(contact);
        }
        return contact;
    }

    // 加载联系人的详细信息（名字和电话号码）
    private void loadContactDetails(Contact contact) {
        List<String> names = contactMapper.findNamesByContactId(contact.getId());
        List<String> phoneNumbers = contactMapper.findPhoneNumbersByContactId(contact.getId());
        contact.setNames(names);
        contact.setPhoneNumbers(phoneNumbers);
    }

    // 创建新联系人
    @Transactional
    public Contact createContact(Contact contact) {
        // 插入基本信息
        contactMapper.insert(contact);

        // 插入名字
        if (contact.getNames() != null) {
            for (String name : contact.getNames()) {
                contactMapper.insertName(contact.getId(), name);
            }
        }

        // 插入电话号码
        if (contact.getPhoneNumbers() != null) {
            for (String phoneNumber : contact.getPhoneNumbers()) {
                contactMapper.insertPhoneNumber(contact.getId(), phoneNumber);
            }
        }

        return contact;
    }

    // 更新联系人
    @Transactional
    public void updateContact(Contact contact) {
        // 更新基本信息
        contactMapper.update(contact);

        // 更新名字
        contactMapper.deleteNames(contact.getId());
        if (contact.getNames() != null) {
            for (String name : contact.getNames()) {
                contactMapper.insertName(contact.getId(), name);
            }
        }

        // 更新电话号码
        contactMapper.deletePhoneNumbers(contact.getId());
        if (contact.getPhoneNumbers() != null) {
            for (String phoneNumber : contact.getPhoneNumbers()) {
                contactMapper.insertPhoneNumber(contact.getId(), phoneNumber);
            }
        }
    }

    // 删除联系人
    @Transactional
    public void deleteContact(Long id) {
        // 由于设置了外键级联删除，只需删除主表数据
        contactMapper.delete(id);
    }

    // 获取收藏的联系人
    public List<Contact> getFavoriteContacts() {
        List<Contact> favorites = contactMapper.findFavorites();
        for (Contact contact : favorites) {
            loadContactDetails(contact);
        }
        return favorites;
    }

    // 更新收藏状态
    public void updateFavoriteStatus(Long id, boolean favorite) {
        contactMapper.updateFavorite(id, favorite);
    }

    // 搜索联系人
    public List<Contact> searchContacts(String keyword) {
        List<Contact> results = new ArrayList<>();
        // 搜索名字
        results.addAll(contactMapper.searchByName(keyword));
        // 搜索电话号码
        results.addAll(contactMapper.searchByPhoneNumber(keyword));

        // 去重并加载详细信息
        List<Contact> uniqueResults = results.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Contact contact : uniqueResults) {
            loadContactDetails(contact);
        }

        return uniqueResults;
    }

    // 导出到Excel
    public ByteArrayInputStream exportToExcel() throws IOException {
        List<Contact> contacts = getAllContacts();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Contacts");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Names");
            headerRow.createCell(1).setCellValue("Phone Numbers");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Location");
            headerRow.createCell(4).setCellValue("Info");
            headerRow.createCell(5).setCellValue("Favorite");
            headerRow.createCell(6).setCellValue("Social Media");

            // 填充数据
            int rowNum = 1;
            for (Contact contact : contacts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.join(", ", contact.getNames()));
                row.createCell(1).setCellValue(String.join(", ", contact.getPhoneNumbers()));
                row.createCell(2).setCellValue(contact.getEmail());
                row.createCell(3).setCellValue(contact.getLocation());
                row.createCell(5).setCellValue(contact.isFavorite());
                row.createCell(6).setCellValue(contact.getMediaHandles());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // 从Excel导入
    @Transactional
    public void importFromExcel(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Contact contact = new Contact();

                // 处理名字（以逗号分隔的多个名字）
                String namesStr = getCellValueAsString(row.getCell(0));
                if (namesStr != null && !namesStr.trim().isEmpty()) {
                    contact.setNames(Arrays.asList(namesStr.split("\\s*,\\s*")));
                }

                // 处理电话号码（以逗号分隔的多个号码）
                String phonesStr = getCellValueAsString(row.getCell(1));
                if (phonesStr != null && !phonesStr.trim().isEmpty()) {
                    contact.setPhoneNumbers(Arrays.asList(phonesStr.split("\\s*,\\s*")));
                }

                contact.setEmail(getCellValueAsString(row.getCell(2)));
                contact.setLocation(getCellValueAsString(row.getCell(3)));
                contact.setFavorite(Boolean.parseBoolean(getCellValueAsString(row.getCell(5))));
                contact.setMediaHandles(getCellValueAsString(row.getCell(6)));

                createContact(contact);
            }
        }
    }

    // 辅助方法：获取单元格的字符串值
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}