package hu.microsec.cegbir.kocsis.jira

enum class Statuses(val statusName: String) {
    // issue statuses
    // @formatter:off
    // -- start
    NEW("New"),
    APPROVED("Approved"),
    READY("Estimated"),
    // -- work
    IN_PROGRESS("In Progress"),
    FEEDBACK("Feedback"),
    RELEASE("Release"),
    // -- closed
    DONE("Done"),
    // @formatter:on

    // subtask statuses
    // @formatter:off
    TO_DO("ToDo"),
    // IN_PROGRESS("In Progress"),
    CODE_REVIEW("Code review"),
    // DONE("Done"),
    // @formatter:on
}
