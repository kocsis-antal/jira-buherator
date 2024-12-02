package hu.microsec.cegbir.kocsis.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebController(
    val restController: RestController,
) {
    @Value("#{utf8properties['spring.application.name']}")
    var appName: String? = null

    @GetMapping("/")
    fun homePage(model: Model): String {
        model.addAttribute("appName", appName)

        model.addAttribute("moveToReady", restController.moveToReady)
        model.addAttribute("closeRemained", restController.closeRemained)

        return "home"
    }
}
