package com.catmate.volunteer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VolunteerTaskRepository extends JpaRepository<VolunteerTask,Long>{List<VolunteerTask> findAllByOrderByCreatedAtDesc();}
