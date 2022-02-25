package hu.microsec.cegbir.kocsis

enum class Statuses(val statusName: String) {
    // issue statuses
    // @formatter:off
    // -- start
    NEW("New"),
    APPROVED("Approved"),
    READY("Ready"),
    // -- work
    IN_PROGRESS("In Progress"),
    DEMO("Demo"),
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
