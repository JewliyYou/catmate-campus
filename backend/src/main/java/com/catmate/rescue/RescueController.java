package com.catmate.rescue;

import com.catmate.common.ApiException;
import com.catmate.user.UserAccount;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/rescues")
public class RescueController {
    private final RescueTaskRepository repository;
    public RescueController(RescueTaskRepository repository){this.repository=repository;}
    @GetMapping public List<RescueTask> list(){return repository.findAllByOrderByCreatedAtDesc();}
    @PostMapping public RescueTask create(@Valid @RequestBody RescueTask task){task.setStatus("待接单");task.setOwnerName("暂未指派");return repository.save(task);}
    @PatchMapping("/{id}/accept") public RescueTask accept(@PathVariable Long id,@RequestAttribute("currentUser") UserAccount user){RescueTask task=repository.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"救助任务不存在"));if(!"待接单".equals(task.getStatus()))throw new ApiException(HttpStatus.CONFLICT,"任务已被接单");task.setStatus("前往中");task.setOwnerName(user.getDisplayName());return repository.save(task);}
    @PutMapping("/{id}") public RescueTask update(@PathVariable Long id,@Valid @RequestBody RescueTask input){RescueTask task=get(id);task.setCatName(input.getCatName());task.setTitle(input.getTitle());task.setArea(input.getArea());task.setPriority(input.getPriority());task.setStatus(input.getStatus());task.setOwnerName(input.getOwnerName());return repository.save(task);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){repository.delete(get(id));}
    private RescueTask get(Long id){return repository.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"救助任务不存在"));}
}
