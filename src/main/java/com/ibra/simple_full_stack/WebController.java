package  com.ibra.simple_full_stack;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WebController {
    @GetMapping("/")
    public String index() {
        return "index.html"; // This will resolve to index.html
    }
}