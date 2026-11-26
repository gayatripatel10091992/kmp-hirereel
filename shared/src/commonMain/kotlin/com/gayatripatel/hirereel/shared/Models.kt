package com.gayatripatel.hirereel.shared

enum class JobType(val label: String) { FULL_TIME("Full-time"), PART_TIME("Part-time"), CONTRACT("Contract"), INTERNSHIP("Internship") }

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val city: String,
    val remote: Boolean,
    val type: JobType,
    val salaryMinK: Int,
    val salaryMaxK: Int,
    val postedAtMillis: Long,
    val applicants: Int,
    val tags: List<String>,
    val description: String,
    /** Recruiter's intro video, streamed in the feed. */
    val videoUrl: String,
    /** Two colours used for the poster frame until the first video frame renders. */
    val posterColors: Pair<Long, Long>,
    val questions: List<InterviewQuestion>,
)

data class InterviewQuestion(
    val id: Int,
    val prompt: String,
    val maxSeconds: Int,
    val prepSeconds: Int = 3,
    val retakesAllowed: Int = 2,
)

enum class ApplicationStatus(val label: String) {
    DRAFT("Draft"),
    UPLOADING("Uploading"),
    WAITING_FOR_NETWORK("Waiting for Wi-Fi"),
    SUBMITTED("Submitted"),
    VIEWED("Viewed by recruiter"),
    SHORTLISTED("Shortlisted"),
    NOT_SELECTED("Not selected"),
}

data class JobApplication(
    val id: String,
    val jobId: String,
    val status: ApplicationStatus,
    val answers: Int,
    val totalBytes: Long,
    val uploadedBytes: Long,
    val updatedAtMillis: Long,
) {
    val progress: Float get() = if (totalBytes <= 0) 0f else (uploadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
}
