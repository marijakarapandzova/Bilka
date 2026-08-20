package com.plantpulse.plantservice.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun home(): String = "redirect:/swagger-ui.html"
}
