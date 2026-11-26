package com.gayatripatel.hirereel.shared

/**
 * Demo jobs. Companies are fictional; the clips are Google's public sample videos standing in
 * for recruiter intro videos.
 */
object SampleJobs {
    /** 2026-11-20T10:00Z, used by tests and screenshots. */
    const val FIXED_NOW: Long = 1_795_168_800_000L

    private const val H = 3_600_000L
    private const val CLIPS = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/"

    private val standardQuestions = listOf(
        InterviewQuestion(1, "Introduce yourself and tell us about a project you're proud of.", maxSeconds = 60),
        InterviewQuestion(2, "Why does this role interest you?", maxSeconds = 45),
        InterviewQuestion(3, "Describe a time you handled a difficult customer or teammate.", maxSeconds = 90),
    )

    fun jobs(now: Long): List<Job> = listOf(
        Job(
            "j1", "Senior Android Engineer", "Northwind Health", "Austin, TX", remote = true, JobType.FULL_TIME,
            140, 175, now - 3 * H, 86, listOf("Kotlin", "Compose", "Healthcare"),
            "Build our patient app used by 2M people. You'll own video visits, offline sync and accessibility.",
            CLIPS + "ForBiggerBlazes.mp4", 0xFF3949ABL to 0xFF00ACC1L, standardQuestions,
        ),
        Job(
            "j2", "Store Manager", "Bluebird Coffee", "Seattle, WA", remote = false, JobType.FULL_TIME,
            58, 72, now - 20 * H, 214, listOf("Retail", "Leadership"),
            "Lead a team of 12 baristas, own scheduling and customer experience for our flagship store.",
            CLIPS + "ForBiggerEscapes.mp4", 0xFF6D4C41L to 0xFFFFA726L, standardQuestions.take(2),
        ),
        Job(
            "j3", "Delivery Partner", "QuickCart", "Chicago, IL", remote = false, JobType.PART_TIME,
            30, 42, now - 2 * 24 * H, 1_380, listOf("Flexible hours", "Driving"),
            "Choose your own shifts and deliver groceries across the North Side. Weekly payouts.",
            CLIPS + "ForBiggerFun.mp4", 0xFF2E7D32L to 0xFFC0CA33L, standardQuestions.take(1),
        ),
        Job(
            "j4", "Product Designer", "Lumen Labs", "New York, NY", remote = true, JobType.CONTRACT,
            95, 130, now - 4 * 24 * H, 7, listOf("Figma", "Design systems"),
            "Six-month contract to shape the design system for a fintech launch.",
            CLIPS + "ForBiggerJoyrides.mp4", 0xFFAD1457L to 0xFF7E57C2L, standardQuestions,
        ),
        Job(
            "j5", "Customer Support Associate", "Harbor Telecom", "Denver, CO", remote = true, JobType.FULL_TIME,
            45, 55, now - 9 * 24 * H, 512, listOf("Support", "Night shift"),
            "Help customers by chat and phone. Paid training, full benefits from day one.",
            CLIPS + "ForBiggerMeltdowns.mp4", 0xFF00695CL to 0xFF26A69AL, standardQuestions.take(2),
        ),
    )

    fun applications(now: Long): List<JobApplication> = listOf(
        JobApplication("a1", "j1", ApplicationStatus.UPLOADING, 3, 48_300_000, 19_400_000, now - 5 * 60_000),
        JobApplication("a2", "j4", ApplicationStatus.WAITING_FOR_NETWORK, 3, 52_100_000, 8_000_000, now - 40 * 60_000),
        JobApplication("a3", "j2", ApplicationStatus.SHORTLISTED, 2, 31_000_000, 31_000_000, now - 26 * H),
        JobApplication("a4", "j5", ApplicationStatus.VIEWED, 2, 29_600_000, 29_600_000, now - 3 * 24 * H),
        JobApplication("a5", "j3", ApplicationStatus.NOT_SELECTED, 1, 12_800_000, 12_800_000, now - 6 * 24 * H),
    )
}
