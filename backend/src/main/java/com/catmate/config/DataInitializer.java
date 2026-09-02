package com.catmate.config;

import com.catmate.auth.PasswordService;
import com.catmate.cat.*;
import com.catmate.rescue.*;
import com.catmate.user.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserAccountRepository users; private final PasswordService passwords; private final CatRepository cats; private final RescueTaskRepository rescues; private final ObjectMapper objectMapper;
    private final boolean seedDemoUsers; private final String initialAdminUsername; private final String initialAdminPassword; private final String initialAdminDisplayName;
    public DataInitializer(UserAccountRepository users,PasswordService passwords,CatRepository cats,RescueTaskRepository rescues,ObjectMapper objectMapper,
        @Value("${catmate.seed-demo-users:true}") boolean seedDemoUsers,
        @Value("${catmate.initial-admin.username:}") String initialAdminUsername,
        @Value("${catmate.initial-admin.password:}") String initialAdminPassword,
        @Value("${catmate.initial-admin.display-name:平台管理员}") String initialAdminDisplayName){
        this.users=users;this.passwords=passwords;this.cats=cats;this.rescues=rescues;this.objectMapper=objectMapper;
        this.seedDemoUsers=seedDemoUsers;this.initialAdminUsername=initialAdminUsername.trim();this.initialAdminPassword=initialAdminPassword;this.initialAdminDisplayName=initialAdminDisplayName.trim();
    }
    private static final List<String> LEGACY_DEMO_CAT_CODES=List.of("CAT-2026-001","CAT-2026-002","CAT-2026-003","CAT-2026-004","CAT-2025-012","CAT-2026-005");
    @Override @Transactional public void run(String... args){seedUsers();seedCats();removeLegacyRescues();}
    private void seedUsers(){
        if(seedDemoUsers){createUser("user","123456","普通用户",UserAccount.Role.USER);createUser("admin","admin123","平台管理者",UserAccount.Role.ADMIN);}
        if(initialAdminUsername.isBlank()&&initialAdminPassword.isBlank()){
            if(!seedDemoUsers)throw new IllegalStateException("关闭演示账号后，必须配置 INITIAL_ADMIN_USERNAME 和 INITIAL_ADMIN_PASSWORD");
            return;
        }
        if(initialAdminUsername.isBlank()||initialAdminPassword.isBlank())throw new IllegalStateException("INITIAL_ADMIN_USERNAME 和 INITIAL_ADMIN_PASSWORD 必须同时配置");
        if(!initialAdminUsername.matches("^[A-Za-z0-9_]{3,20}$"))throw new IllegalStateException("INITIAL_ADMIN_USERNAME 须为3至20位字母、数字或下划线");
        if(initialAdminPassword.length()<12)throw new IllegalStateException("INITIAL_ADMIN_PASSWORD 至少需要12个字符");
        createUser(initialAdminUsername,initialAdminPassword,initialAdminDisplayName.isBlank()?"平台管理员":initialAdminDisplayName,UserAccount.Role.ADMIN);
    }
    private void createUser(String username,String password,String name,UserAccount.Role role){if(users.findByUsername(username).isPresent())return;UserAccount u=new UserAccount();u.setUsername(username);u.setPasswordHash(passwords.hash(password));u.setDisplayName(name);u.setRole(role);users.save(u);}
    private void seedCats(){
        cats.deleteByCodeIn(LEGACY_DEMO_CAT_CODES);
        try(var input=new ClassPathResource("cat-data.json").getInputStream()){
            List<Cat> imported=objectMapper.readValue(input,new TypeReference<List<Cat>>(){});
            for(Cat source:imported){
                Cat target=cats.findByCode(source.getCode()).orElseGet(Cat::new);
                copyImportedCat(source,target);
                cats.save(target);
            }
        }catch(Exception exception){throw new IllegalStateException("无法导入猫咪数据",exception);}
    }
    private void copyImportedCat(Cat source,Cat target){target.setCode(source.getCode());target.setName(source.getName());target.setSex(source.getSex());target.setEnrollmentTime(source.getEnrollmentTime());target.setArea(source.getArea());target.setStatus(source.getStatus());target.setSchoolStatus(source.getSchoolStatus());target.setHealth(source.getHealth());target.setPersonality(source.getPersonality());target.setAppearance(source.getAppearance());target.setNotes(source.getNotes());target.setFriendliness(source.getFriendliness());target.setImageUrl(source.getImageUrl());target.setImageUrls(source.getImageUrls());}
    private void removeLegacyRescues(){rescues.deleteByCatNameAndTitleAndAreaAndOwnerName("花卷","左后腿疑似受伤，需要送医","行政楼花坛","志愿者 林夏");rescues.deleteByCatNameAndTitleAndAreaAndOwnerName("煤球","连续两天食欲下降，请协助观察","三号宿舍楼","暂未指派");rescues.deleteByCatNameAndTitleAndAreaAndOwnerName("未建档幼猫","排水沟附近发现幼猫叫声","体育馆南侧","志愿者 陈默");}
}
