package com.catmate.volunteer;

import com.catmate.common.ApiException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/volunteers")
public class VolunteerController {
    private final VolunteerTaskRepository repository;
    public VolunteerController(VolunteerTaskRepository repository){this.repository=repository;}
    @GetMapping public List<VolunteerTask> list(){return repository.findAllByOrderByCreatedAtDesc();}
    @PostMapping public VolunteerTask create(@Valid @RequestBody VolunteerTask input){if(input.getOwnerName()==null||input.getOwnerName().isBlank())input.setOwnerName("待认领");if(input.getStatus()==null||input.getStatus().isBlank())input.setStatus("待安排");return repository.save(input);}
    @PutMapping("/{id}") public VolunteerTask update(@PathVariable Long id,@Valid @RequestBody VolunteerTask input){VolunteerTask task=get(id);task.setTitle(input.getTitle());task.setScheduleText(input.getScheduleText());task.setOwnerName(input.getOwnerName());task.setStatus(input.getStatus());task.setNotes(input.getNotes());return repository.save(task);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){repository.delete(get(id));}
    private VolunteerTask get(Long id){return repository.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"志愿任务不存在"));}
}
