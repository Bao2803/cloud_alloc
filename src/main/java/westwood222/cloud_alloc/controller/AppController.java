package westwood222.cloud_alloc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class AppController {
    @GetMapping
    public RedirectView goHome() {
        return new RedirectView("/home");
    }

    @GetMapping("home")
    public String home() {
        return "Hello, World!";
    }
}
