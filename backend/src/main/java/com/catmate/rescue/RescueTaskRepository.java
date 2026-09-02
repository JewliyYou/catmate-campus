package com.catmate.rescue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RescueTaskRepository extends JpaRepository<RescueTask,Long>{List<RescueTask> findAllByOrderByCreatedAtDesc();long countByStatusNot(String status);long deleteByCatNameAndTitleAndAreaAndOwnerName(String catName,String title,String area,String ownerName);}
