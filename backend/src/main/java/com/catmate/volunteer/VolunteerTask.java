package com.catmate.volunteer;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity @Table(name="volunteer_task")
public class VolunteerTask {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @NotBlank(message="请输入志愿任务") @Column(nullable=false,length=100) private String title;
    @NotBlank(message="请输入服务时间") @Column(name="schedule_text",nullable=false,length=100) private String scheduleText;
    @Column(name="owner_name",length=50) private String ownerName="待认领";
    @Column(nullable=false,length=30) private String status="待安排";
    @Column(length=500) private String notes;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    public Long getId(){return id;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getScheduleText(){return scheduleText;} public void setScheduleText(String v){scheduleText=v;} public String getOwnerName(){return ownerName;} public void setOwnerName(String v){ownerName=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
