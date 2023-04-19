package hu.microsec.cegbir.kocsis.helper

import hu.microsec.cegbir.kocsis.jira.JiraBuherator
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.CC_PROJECTS_FILTER
import hu.microsec.cegbir.kocsis.jira.JiraBuherator.Companion.DALX_PROJECTS
import hu.microsec.cegbir.kocsis.jira.Statuses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Sprint(val jiraBuherator: JiraBuherator) {
    fun moveToReady() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = jiraBuherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        println("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            jiraBuherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            if (jiraBuherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)) {
                logger.info("[${it.key}] moved to ${Statuses.READY}")
                println("[${it.key}] moved to ${Statuses.READY}")
            } else logger.warn("Can't move [${it.key}] (${it.status.name})")
        }
    }

    fun moveEpics() {
        val toTranslate = arrayOf(Statuses.NEW.statusName, Statuses.APPROVED.statusName)
        val result = jiraBuherator.select("""filter = "CC projects issues in open sprint" AND status in (${toTranslate.joinToString()})""")
        println("Found ${result.total} issues.")
        result.issues.filter { toTranslate.contains(it.status.name) }.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")
            jiraBuherator.moveIssue(it, Statuses.NEW, Statuses.APPROVED)
            if (jiraBuherator.moveIssue(it, Statuses.APPROVED, Statuses.READY)) {
                logger.info("[${it.key}] moved to ${Statuses.READY}")
                println("[${it.key}] moved to ${Statuses.READY}")
            } else logger.warn("Can't move [${it.key}] (${it.status.name})")
        }
    }

    fun closeRemained() {
        val result = jiraBuherator.select("""$CC_PROJECTS_FILTER AND type in standardIssueTypes() AND status = "${Statuses.IN_PROGRESS.statusName}" AND issuefunction in hasSubtasks() AND NOT issuefunction in parentsOf("status != ${Statuses.DONE.statusName}")""")
        println("Found ${result.total} issues.")
        result.issues.forEach {
            println("Transferring [${it.key}] from ${it.status.name}")

            if (DALX_PROJECTS.contains(it.project.key) && (it.fixVersions?.count() ?: 0) == 0) {
                logger.info("[${it.key}] adding fix version development") // TODO
            }

            if (jiraBuherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.FEEDBACK) || jiraBuherator.moveIssue(it, Statuses.IN_PROGRESS, Statuses.RELEASE)) {
                logger.info("[${it.key}] closed")
                println("[${it.key}] closed")
            } else logger.warn("Can't close [${it.key}](${it.assignee?.name}) (${it.status.name})")
        }
    }

    fun createRovatIssues(parent: String) {
        val parentIssue = jiraBuherator.getIssue(parent)

        // java
        val xsd = jiraBuherator.createMainTaskWithParent(parent, "CEGELJARAS", "${parentIssue.summary} - nyomtatvány", "nyomtatvány XSD és XSLT kialakítása\nXSD-ből DTO-k kialakítása")

        val portal = jiraBuherator.createMainTaskWithParent(parent, "CEGPORTAL", "${parentIssue.summary} - cégportál", "nyomtatvány XSD linkek frissítése")
        val portalElesites = jiraBuherator.createReleaseTsask(portal, "CEGPORTAL")
        jiraBuherator.linkBlock(xsd, portal)

        val beadvany = jiraBuherator.createMainTaskWithParent(parent, "CEGELJARAS", "${parentIssue.summary} - beadvány", "sémaspecifikus DTO-k változásának átvezetése a közös DTO-kba")
        jiraBuherator.linkBlock(xsd, beadvany)

        val infovizsg = jiraBuherator.createMainTaskWithParent(parent, "INFOVIZSG", "${parentIssue.summary} - infóvizsgálat", "új ellenőrzések kialakítása\n" + "meglévő ellenőrzések módosítása\n" + "új sémák elfogadása")
        val infovizsgElesites = jiraBuherator.createReleaseTsask(infovizsg, "INFOVIZSG")
        jiraBuherator.linkBlock(beadvany, infovizsg)

        val kiadmany = jiraBuherator.createMainTaskWithParent(parent, "KIADMANY", "${parentIssue.summary} - kiadmány", "DTO-k módosítása\n" + "végzéstörzsek kialakítása")
        jiraBuherator.linkBlock(beadvany, kiadmany)

        val cet = jiraBuherator.createMainTaskWithParent(parent, "HARMASKA", "${parentIssue.summary} - CET", "SQL séma módosítása\n" + "adatbáziskezelő réteg (entity, DAO)\n" + "komponensek frissítése\n" + "CgRovatTipus\n" + "végzéstörzs - RovatMapper")
        val cetElesites = jiraBuherator.createReleaseTsask(cet, "HARMASKA")
        jiraBuherator.linkBlock(beadvany, cet)

        val bris = jiraBuherator.createMainTaskWithParent(parent, "BRIS", "${parentIssue.summary} - BRIS", "DTO-k módosítása\n" + "üzenetek kialakítása")
        val brisElesites = jiraBuherator.createReleaseTsask(cet, "BRIS")
        jiraBuherator.linkBlock(cet, bris)

        // dalx
        jiraBuherator.createMainTaskWithParent(parent, "EGYABLAK", "${parentIssue.summary} - egyablak", "adatigény modellek")
        jiraBuherator.createMainTaskWithParent(parent, "CEGELJARAS", "${parentIssue.summary} - cegeljaras", "séma")
        jiraBuherator.createMainTaskWithParent(parent, "CEGHIRNOK", "${parentIssue.summary} - ceghirnok", "adatigény modellek")
        jiraBuherator.createMainTaskWithParent(parent, "KETTESKE", "${parentIssue.summary} - ketteske", "sql-ek\n" + "szerkesztő felület\n" + "előszerkesztés	áttétel\n" + "adatigény modellek")
        jiraBuherator.createMainTaskWithParent(parent, "CEGKOZPONT", "${parentIssue.summary} - cegkozpont", "sql-ek")
        jiraBuherator.createMainTaskWithParent(parent, "CEGINFO", "${parentIssue.summary} - ceginfo", "html megjelenítő\n" + "xml megjelenítő\n" + "fordítások")
        jiraBuherator.createMainTaskWithParent(parent, "AFSZ", "${parentIssue.summary} - afsz", "sql-ek\n" + "html megjelenítő")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Sprint::class.java)
    }
}
