package hu.microsec.cegbir.kocsis.web

import hu.microsec.cegbir.kocsis.helper.Sprint
import hu.microsec.cegbir.kocsis.helper.Sprint.SprintResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RestController(
    val sprintHelper: Sprint,
) {
    var moveToReady: SprintResult? = null
    var closeRemained: SprintResult? = null

    @GetMapping(params = ["move"])
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    fun move() {
        moveToReady = sprintHelper.moveToReady()
    }

    @GetMapping(params = ["close"])
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun close() {
        closeRemained = sprintHelper.closeRemained()
    }
}
