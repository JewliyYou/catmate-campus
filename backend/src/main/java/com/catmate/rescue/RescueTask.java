package com.catmate.rescue;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity @Table(name="rescue_task")
public class RescueTask {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @NotBlank(message="请选择关联猫咪") @Column(nullable=false,length=50) private String catName;
    @NotBlank(message="请输入异常描述") @Column(nullable=false,length=500) private String title;
    @NotBlank(message="请输入发现位置") @Column(nullable=false) private String area;
    @Column(nullable=false,length=10) private String priority="MEDIUM";
    @Column(nullable=false,length=30) private String status="待接单";
    @Column(length=50) private String ownerName="暂未指派";
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    public Long getId(){return id;} public String getCatName(){return catName;} public void setCatName(String v){catName=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getArea(){return area;} public void setArea(String v){area=v;} public String getPriority(){return priority;} public void setPriority(String v){priority=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getOwnerName(){return ownerName;} public void setOwnerName(String v){ownerName=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
