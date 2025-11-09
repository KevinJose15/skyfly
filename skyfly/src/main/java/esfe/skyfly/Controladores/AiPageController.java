package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiPageController {

  @GetMapping("/ai")
  public String ai() {
    // templates/ai/chat.html
    return "ai/chat";
  }
}
