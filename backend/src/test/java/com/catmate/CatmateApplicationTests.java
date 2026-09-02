package com.catmate;

import static org.assertj.core.api.Assertions.assertThat;
import com.catmate.cat.CatRepository;
import com.catmate.cat.Cat;
import com.catmate.config.DataInitializer;
import com.catmate.rescue.RescueTask;
import com.catmate.rescue.RescueTaskRepository;
import com.catmate.user.UserAccountRepository;
import com.catmate.volunteer.VolunteerTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatmateApplicationTests {
    @Autowired UserAccountRepository users;
    @Autowired CatRepository cats;
    @Autowired RescueTaskRepository rescues;
    @Autowired VolunteerTaskRepository volunteers;
    @Autowired DataInitializer initializer;
    @Test void contextLoadsAndSeedsAccounts(){assertThat(users.findByUsername("user")).isPresent();assertThat(users.findByUsername("admin")).isPresent();}
    @Test void importsSpreadsheetCats(){assertThat(cats.count()).isEqualTo(117);assertThat(cats.findByCode("CAT-DATA-001")).get().extracting("name","schoolStatus","imageUrl").containsExactly("梨花","在校","/cat-images/cat-001-01.webp");assertThat(cats.findByCode("CAT-DATA-001")).get().extracting(cat->cat.getImageUrls().size()).isEqualTo(6);}
    @Test void removesOnlyLegacyDemoCats() throws Exception {Cat demo=new Cat();demo.setCode("CAT-2026-001");demo.setName("旧演示猫");cats.save(demo);initializer.run();assertThat(cats.existsByCode("CAT-2026-001")).isFalse();assertThat(cats.count()).isEqualTo(117);}
    @Test void removesLegacyRescueHistoryAndStartsWithoutVolunteerHistory() throws Exception {RescueTask demo=new RescueTask();demo.setCatName("花卷");demo.setTitle("左后腿疑似受伤，需要送医");demo.setArea("行政楼花坛");demo.setOwnerName("志愿者 林夏");rescues.save(demo);initializer.run();assertThat(rescues.findAll()).isEmpty();assertThat(volunteers.findAll()).isEmpty();}
}
