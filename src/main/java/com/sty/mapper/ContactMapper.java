package com.sty.mapper;

import com.sty.entity.Contact;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface ContactMapper {
    // 查询所有联系人
    @Select("SELECT * FROM contact")
    List<Contact> findAll();

    // 根据ID查询联系人及其所有名字和电话
    @Select("SELECT * FROM contact WHERE id = #{id}")
    Contact findById(Long id);

    // 查询联系人的所有名字
    @Select("SELECT name FROM contact_names WHERE contact_id = #{contactId}")
    List<String> findNamesByContactId(Long contactId);

    // 查询联系人的所有电话号码
    @Select("SELECT phone_number FROM contact_phones WHERE contact_id = #{contactId}")
    List<String> findPhoneNumbersByContactId(Long contactId);

    // 插入新联系人
    @Insert("INSERT INTO contact (email, location, info, favorite, media_handles) " +
            "VALUES (#{email}, #{location}, #{info}, #{favorite}, #{mediaHandles})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Contact contact);

    // 插入联系人名字
    @Insert("INSERT INTO contact_names (contact_id, name) VALUES (#{contactId}, #{name})")
    void insertName(@Param("contactId") Long contactId, @Param("name") String name);

    // 插入联系人电话
    @Insert("INSERT INTO contact_phones (contact_id, phone_number) VALUES (#{contactId}, #{phoneNumber})")
    void insertPhoneNumber(@Param("contactId") Long contactId, @Param("phoneNumber") String phoneNumber);

    // 更新联系人基本信息
    @Update("UPDATE contact SET email = #{email}, location = #{location}, " +
            "info = #{info}, favorite = #{favorite}, media_handles = #{mediaHandles} " +
            "WHERE id = #{id}")
    void update(Contact contact);

    // 删除联系人的所有名字
    @Delete("DELETE FROM contact_names WHERE contact_id = #{contactId}")
    void deleteNames(Long contactId);

    // 删除联系人的所有电话号码
    @Delete("DELETE FROM contact_phones WHERE contact_id = #{contactId}")
    void deletePhoneNumbers(Long contactId);

    // 删除联系人及其所有相关信息
    @Delete("DELETE FROM contact WHERE id = #{id}")
    void delete(Long id);

    // 查找收藏的联系人
    @Select("SELECT * FROM contact WHERE favorite = true")
    List<Contact> findFavorites();

    // 根据名字搜索联系人
    @Select("SELECT DISTINCT c.* FROM contact c " +
            "JOIN contact_names cn ON c.id = cn.contact_id " +
            "WHERE cn.name LIKE CONCAT('%', #{name}, '%')")
    List<Contact> searchByName(String name);

    // 根据电话号码搜索联系人
    @Select("SELECT DISTINCT c.* FROM contact c " +
            "JOIN contact_phones cp ON c.id = cp.contact_id " +
            "WHERE cp.phone_number LIKE CONCAT('%', #{phoneNumber}, '%')")
    List<Contact> searchByPhoneNumber(String phoneNumber);

    // 更新收藏状态
    @Update("UPDATE contact SET favorite = #{favorite} WHERE id = #{id}")
    void updateFavorite(@Param("id") Long id, @Param("favorite") boolean favorite);

    // 批量插入联系人（用于导入功能）
    @Insert({
        "<script>",
        "INSERT INTO contact (email, location, info, favorite, media_handles) VALUES ",
        "<foreach collection='list' item='contact' separator=','>",
        "(#{contact.email}, #{contact.location}, #{contact.info}, " +
        "#{contact.favorite}, #{contact.mediaHandles})",
        "</foreach>",
        "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void batchInsert(List<Contact> contacts);
}