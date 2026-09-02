package com.catmate.cat;

import com.catmate.common.ApiException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/cats")
public class CatController {
    private final CatRepository repository;
    public CatController(CatRepository repository){this.repository=repository;}
    @GetMapping public List<Cat> list(){return repository.findAll();}
    @GetMapping("/{id}") public Cat get(@PathVariable Long id){return repository.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"猫咪档案不存在"));}
    @PostMapping public Cat create(@Valid @RequestBody Cat cat){cat.setCode("CAT-"+java.time.Year.now().getValue()+"-"+String.format("%03d",repository.count()+1));if(cat.getStatus()==null)cat.setStatus("待确认");return repository.save(cat);}
    @PutMapping("/{id}") public Cat update(@PathVariable Long id,@Valid @RequestBody Cat input){Cat c=get(id);c.setName(input.getName());c.setAliases(input.getAliases());c.setSex(input.getSex());c.setAgeText(input.getAgeText());c.setArea(input.getArea());c.setStatus(input.getStatus());c.setHealth(input.getHealth());c.setPersonality(input.getPersonality());c.setEnrollmentTime(input.getEnrollmentTime());c.setSchoolStatus(input.getSchoolStatus());c.setAppearance(input.getAppearance());c.setNotes(input.getNotes());c.setFriendliness(input.getFriendliness());c.setImageUrl(input.getImageUrl());c.setImageUrls(input.getImageUrls());c.setMapX(input.getMapX());c.setMapY(input.getMapY());return repository.save(c);}
}
