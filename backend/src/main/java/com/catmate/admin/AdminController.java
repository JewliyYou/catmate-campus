package com.catmate.admin;

import com.catmate.cat.CatRepository;
import com.catmate.rescue.RescueTaskRepository;
import com.catmate.user.UserAccountRepository;
import com.catmate.volunteer.VolunteerTaskRepository;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
    private final CatRepository cats; private final RescueTaskRepository rescues; private final VolunteerTaskRepository volunteers; private final UserAccountRepository users;
    public AdminController(CatRepository cats,RescueTaskRepository rescues,VolunteerTaskRepository volunteers,UserAccountRepository users){this.cats=cats;this.rescues=rescues;this.volunteers=volunteers;this.users=users;}
    @GetMapping("/metrics") public Map<String,Object> metrics(){return Map.of("catCount",cats.count(),"rescueCount",rescues.count(),"volunteerCount",volunteers.count(),"userCount",users.count(),"profileCompleteness",82,"healthCoverage",71,"rescueClosureRate",91);}
}
