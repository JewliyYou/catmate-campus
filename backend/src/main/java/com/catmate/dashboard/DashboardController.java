package com.catmate.dashboard;

import com.catmate.cat.CatRepository;
import com.catmate.rescue.RescueTaskRepository;
import com.catmate.volunteer.VolunteerTaskRepository;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final CatRepository cats; private final RescueTaskRepository rescues; private final VolunteerTaskRepository volunteers;
    public DashboardController(CatRepository cats,RescueTaskRepository rescues,VolunteerTaskRepository volunteers){this.cats=cats;this.rescues=rescues;this.volunteers=volunteers;}
    @GetMapping public Map<String,Object> stats(){return Map.of("cats",cats.count(),"activeRescues",rescues.countByStatusNot("已完成"),"volunteerTasks",volunteers.count());}
}
