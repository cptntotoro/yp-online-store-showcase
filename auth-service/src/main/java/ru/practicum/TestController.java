//package ru.practicum;
//
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/test")
//public class TestController {
//    @GetMapping
//    public String test() {
//        return "Auth service is working!";
//    }
//
//    @GetMapping("/protected")
//    public String protectedEndpoint(@AuthenticationPrincipal OidcUser user) {
//        return "Protected data for: " + user.getSubject();
//    }
//}
