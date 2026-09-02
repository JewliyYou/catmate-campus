package com.catmate.cat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="campus_cat")
public class Cat {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String code;
    @NotBlank(message="请输入猫咪名称") @Column(nullable=false, length=50) private String name;
    private String aliases;
    @Column(length=10) private String sex;
    @Column(name="age_text", length=30) private String ageText;
    private String area;
    @Column(length=255) private String status;
    private String health;
    @Column(length=1000) private String personality;
    @Column(name="enrollment_time", length=30) private String enrollmentTime;
    @Column(name="school_status", length=255) private String schoolStatus;
    @Column(length=1000) private String appearance;
    @Column(length=3000) private String notes;
    @Column(name="friendliness") private Integer friendliness;
    @Column(name="image_url", length=255) private String imageUrl;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="campus_cat_image",joinColumns=@JoinColumn(name="cat_id"))
    @OrderColumn(name="sort_order")
    @Column(name="image_url",nullable=false,length=255)
    private List<String> imageUrls = new ArrayList<>();
    @Column(name="map_x") private Integer mapX;
    @Column(name="map_y") private Integer mapY;
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId(){return id;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getAliases(){return aliases;} public void setAliases(String v){aliases=v;} public String getSex(){return sex;} public void setSex(String v){sex=v;} public String getAgeText(){return ageText;} public void setAgeText(String v){ageText=v;} public String getArea(){return area;} public void setArea(String v){area=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getHealth(){return health;} public void setHealth(String v){health=v;} public String getPersonality(){return personality;} public void setPersonality(String v){personality=v;} public String getEnrollmentTime(){return enrollmentTime;} public void setEnrollmentTime(String v){enrollmentTime=v;} public String getSchoolStatus(){return schoolStatus;} public void setSchoolStatus(String v){schoolStatus=v;} public String getAppearance(){return appearance;} public void setAppearance(String v){appearance=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public Integer getFriendliness(){return friendliness;} public void setFriendliness(Integer v){friendliness=v;} public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;} public List<String> getImageUrls(){return imageUrls;} public void setImageUrls(List<String> v){imageUrls=v==null?new ArrayList<>():new ArrayList<>(v);} public Integer getMapX(){return mapX;} public void setMapX(Integer v){mapX=v;} public Integer getMapY(){return mapY;} public void setMapY(Integer v){mapY=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
