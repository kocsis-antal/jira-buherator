package hu.microsec.cegbir.kocsis.helper

import com.atlassian.jira.rest.client.api.domain.Issue
import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.CC_PROJECTS_FILTER
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.DALX_PROJECTS
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class Sprint(val jiraBuherator: JiraBuherator) {
    fun moveToReady(): SprintResult {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = jiraBuherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        logger.debug("Found ${result.total} issues.")

        return SprintResult().apply {
            result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
                logger.debug("Transferring [${it.key}] from ${it.status.name}")
                jiraBuherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
                val error = jiraBuherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)
                if (error == null) {
                    logger.info("[${it.key}] moved to ${Statuses.READY}")
                    logger.info("[${it.key}] moved to ${Statuses.READY}")
                    successfull += it
                } else {
                    logger.warn("Can't move [${it.key}] (${it.status.name})")
                    failed += Pair(it, error)
                }
            }
        }
    }

    fun moveEpics() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = jiraBuherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        logger.debug("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            logger.debug("Transferring [${it.key}] from ${it.status.name}")
            jiraBuherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            val error = jiraBuherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)
            if (error == null) {
                logger.info("[${it.key}] moved to ${Statuses.READY}")
                logger.info("[${it.key}] moved to ${Statuses.READY}")
            } else logger.warn("Can't move [${it.key}] (${it.status.name}): $error")
        }
    }

    fun closeRemained(): SprintResult {
        val result = jiraBuherator.select("""$CC_PROJECTS_FILTER AND project != FKCONNECT AND type in standardIssueTypes() AND status = "${Statuses.IN_PROGRESS.statusName}" AND issuefunction in hasSubtasks() AND NOT issuefunction in parentsOf("status != ${Statuses.DONE.statusName}")""")
        logger.debug("Found ${result.total} issues.")

        return SprintResult().apply {
            result.issues.forEach {
                logger.debug("Transferring [${it.key}] from ${it.status.name}")

                if (DALX_PROJECTS.contains(it.project.key) && (it.fixVersions?.count() ?: 0) == 0) {
                    logger.info("[${it.key}] adding fix version development") // TODO
                }

                val error1 = jiraBuherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.FEEDBACK)
                val error2 = jiraBuherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.RELEASE)
                if (error1 == null || error2 == null) {
                    logger.info("[${it.key}] closed")
                    successfull += it
                } else {
                    logger.warn("Can't close [${it.key}](${it.assignee?.name}) (${it.status.name})")
                    failed += Pair(it, error1)
                }
            }
        }
    }

    fun createRovatIssues(parent: String) {
        val parentIssue = jiraBuherator.getIssue(parent)

        // java
        val xsd = jiraBuherator.createMainTaskWithParent(
            parent, "CEGELJARAS", "${parentIssue.summary} - nyomtatvány", """
            h2.Feladatok
            * nyomtatvány XSD és XSLT kialakítása
            * XSD-ből DTO-k kialakítása
            * catalog.xml frissítése
            """.trimIndent()
        )

        val portal = jiraBuherator.createMainTaskWithParent(
            parent, "CEGPORTAL", "${parentIssue.summary} - cégportál", """
            h2.Feladatok
            * nyomtatvány XSD linkek frissítése
            """.trimIndent()
        )
        val portalElesites = jiraBuherator.createReleaseTsask(portal, "CEGPORTAL")
        jiraBuherator.linkBlock(xsd, portal)

        val beadvany = jiraBuherator.createMainTaskWithParent(
            parent, "CEGELJARAS", "${parentIssue.summary} - beadvány", """
            h2.Feladatok
            * sémaspecifikus DTO-k változásának átvezetése a közös DTO-kba
            """.trimIndent()
        )
        jiraBuherator.linkBlock(xsd, beadvany)

        val infovizsg = jiraBuherator.createMainTaskWithParent(
            parent, "INFOVIZSG", "${parentIssue.summary} - infóvizsgálat", """
            h2.Feladatok
            * új ellenőrzések kialakítása
            * meglévő ellenőrzések módosítása
            * új sémák elfogadása
            """.trimIndent()
        )
        val infovizsgElesites = jiraBuherator.createReleaseTsask(infovizsg, "INFOVIZSG")
        jiraBuherator.linkBlock(beadvany, infovizsg)

        val kiadmany = jiraBuherator.createMainTaskWithParent(
            parent, "KIADMANY", "${parentIssue.summary} - kiadmány", """
            h2.Feladatok
            * DTO-k módosítása
            * végzéstörzsek kialakítása
            """.trimIndent()
        )
        jiraBuherator.linkBlock(beadvany, kiadmany)

        val cet = jiraBuherator.createMainTaskWithParent(
            parent, "HARMASKA", "${parentIssue.summary} - CET", """
            h2.Feladatok
            * SQL séma módosítása
            * adatbáziskezelő réteg (entity, DAO)
            * komponensek frissítése
            * CgRovatTipus
            * végzéstörzs - RovatMapper
            """.trimIndent()
        )
        val cetElesites = jiraBuherator.createReleaseTsask(cet, "HARMASKA")
        jiraBuherator.linkBlock(beadvany, cet)

        val bris = jiraBuherator.createMainTaskWithParent(
            parent, "BRIS", "${parentIssue.summary} - BRIS", """
            h2.Feladatok
            * DTO-k módosítása
            * üzenetek kialakítása
            """.trimMargin()
        )
        val brisElesites = jiraBuherator.createReleaseTsask(cet, "BRIS")
        jiraBuherator.linkBlock(cet, bris)

        // dalx
        jiraBuherator.createMainTaskWithParent(
            parent, "EGYABLAK", "${parentIssue.summary} - egyablak", """
            h2.Feladatok
            * adatigény modellek
            """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "CEGELJARAS", "${parentIssue.summary} - cegeljaras", """
            h2.Feladatok
            * séma
            """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "CEGHIRNOK", "${parentIssue.summary} - ceghirnok", """
            h2.Feladatok
            * adatigény modellek
            """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "KETTESKE", "${parentIssue.summary} - ketteske", """
            h2.Feladatok
            * sql-ek
            * szerkesztő felület
            * előszerkesztés áttétel
            * adatigény modellek
            """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "CEGKOZPONT", "${parentIssue.summary} - cegkozpont", """
            h2.Feladatok
            * sql-ek
            """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "CEGINFO", "${parentIssue.summary} - ceginfo", """
            h2.Feladatok
            * html megjelenítő
            * xml megjelenítő
            * fordítások
           """.trimIndent()
        )
        jiraBuherator.createMainTaskWithParent(
            parent, "AFSZ", "${parentIssue.summary} - afsz", """
            h2.Feladatok
            * sql-ek
            * html megjelenítő
            """.trimIndent()
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Sprint::class.java)
    }

    class SprintResult {
        var time: LocalDateTime = LocalDateTime.now()
        var successfull: List<Issue> = emptyList()
        var failed: List<Pair<Issue, String>> = emptyList()
    }
}
