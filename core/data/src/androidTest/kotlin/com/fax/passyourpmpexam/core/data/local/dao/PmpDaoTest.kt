package com.fax.passyourpmpexam.core.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fax.passyourpmpexam.core.data.local.PmpDatabase
import com.fax.passyourpmpexam.core.data.local.entity.AttemptEntity
import com.fax.passyourpmpexam.core.data.local.entity.QuestionEntity
import com.fax.passyourpmpexam.core.data.local.entity.QuizSessionEntity
import com.fax.passyourpmpexam.core.data.local.entity.SessionQuestionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room round-trips against an in-memory [PmpDatabase]: exercises the DAOs, the JSON list
 * [com.fax.passyourpmpexam.core.data.local.Converters], and the ON DELETE CASCADE / foreign-key
 * enforcement between quiz sessions and their questions.
 */
@RunWith(AndroidJUnit4::class)
class PmpDaoTest {

    private lateinit var db: PmpDatabase
    private lateinit var questionDao: QuestionDao
    private lateinit var sessionDao: QuizSessionDao
    private lateinit var sessionQuestionDao: SessionQuestionDao
    private lateinit var attemptDao: AttemptDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PmpDatabase::class.java).build()
        questionDao = db.questionDao()
        sessionDao = db.quizSessionDao()
        sessionQuestionDao = db.sessionQuestionDao()
        attemptDao = db.attemptDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun questionUpsertRoundTripsOptionsAndFiltersByCertification() = runTest {
        questionDao.upsertAll(
            listOf(
                question("q1", certificationId = "pmp", options = listOf("a", "b", "c", "d")),
                question("q2", certificationId = "pmp"),
                question("other", certificationId = "capm"),
            ),
        )

        val pmp = questionDao.getAll("pmp")
        assertEquals(2, pmp.size)
        assertEquals(listOf("a", "b", "c", "d"), questionDao.getById("q1")?.options)
        assertEquals(3, questionDao.count())
        assertNull(questionDao.getById("missing"))
    }

    @Test
    fun questionUpsertReplacesExistingRow() = runTest {
        questionDao.upsertAll(listOf(question("q1", text = "before")))
        questionDao.upsertAll(listOf(question("q1", text = "after")))

        assertEquals(1, questionDao.count())
        assertEquals("after", questionDao.getById("q1")?.text)
    }

    @Test
    fun attemptCountsAndBetweenWindowAreInclusiveOfStartExclusiveOfEnd() = runTest {
        attemptDao.insertAll(
            listOf(
                attempt("a1", answeredAt = 100L, isCorrect = true),
                attempt("a2", answeredAt = 200L, isCorrect = false),
                attempt("a3", answeredAt = 300L, isCorrect = true),
            ),
        )

        assertEquals(3, attemptDao.totalCount())
        assertEquals(2, attemptDao.correctCount())
        // [100, 300): includes a1 and a2, excludes a3 at the upper bound.
        assertEquals(2, attemptDao.observeCountBetween(100L, 300L).first())
        assertEquals(3, attemptDao.observeAll().first().size)
    }

    @Test
    fun latestSessionByStatusOrdersByUpdatedAtDescending() = runTest {
        sessionDao.upsert(session("s1", status = "IN_PROGRESS", updatedAt = 10L))
        sessionDao.upsert(session("s2", status = "IN_PROGRESS", updatedAt = 30L))
        sessionDao.upsert(session("s3", status = "COMPLETED", updatedAt = 40L))

        assertEquals("s2", sessionDao.getLatestByStatus("IN_PROGRESS")?.id)
        assertEquals("s3", sessionDao.getLatestByStatus("COMPLETED")?.id)
        assertNull(sessionDao.getLatestByStatus("ABANDONED"))
    }

    @Test
    fun sessionQuestionsReturnInOrderIndexOrder() = runTest {
        sessionDao.upsert(session("s1"))
        sessionQuestionDao.upsertAll(
            listOf(
                sessionQuestion("s1", orderIndex = 2, questionId = "q2"),
                sessionQuestion("s1", orderIndex = 0, questionId = "q0"),
                sessionQuestion("s1", orderIndex = 1, questionId = "q1"),
            ),
        )

        val ordered = sessionQuestionDao.getForSession("s1").map { it.questionId }
        assertEquals(listOf("q0", "q1", "q2"), ordered)
    }

    @Test
    fun deletingSessionCascadesToItsQuestions() = runTest {
        sessionDao.upsert(session("s1"))
        sessionQuestionDao.upsertAll(
            listOf(
                sessionQuestion("s1", orderIndex = 0, questionId = "q0"),
                sessionQuestion("s1", orderIndex = 1, questionId = "q1"),
            ),
        )
        assertEquals(2, sessionQuestionDao.getForSession("s1").size)

        // No delete DAO exists yet; delete the parent directly. Room enables foreign_keys by
        // default, so the ON DELETE CASCADE removes the child rows.
        db.openHelper.writableDatabase.execSQL("DELETE FROM quiz_sessions WHERE id = 's1'")

        assertTrue(sessionQuestionDao.getForSession("s1").isEmpty())
    }

    @Test
    fun insertingSessionQuestionForMissingSessionViolatesForeignKey() = runTest {
        // @Upsert re-throws non-uniqueness constraint failures (an FK violation isn't a PK conflict).
        var threw = false
        try {
            sessionQuestionDao.upsertAll(
                listOf(sessionQuestion("ghost", orderIndex = 0, questionId = "q0")),
            )
        } catch (e: SQLiteConstraintException) {
            threw = true
        }
        assertTrue("expected a foreign-key constraint violation", threw)
    }

    private fun question(
        id: String,
        certificationId: String = "pmp",
        text: String = "text",
        options: List<String> = listOf("a", "b", "c", "d"),
    ) = QuestionEntity(
        id = id,
        certificationId = certificationId,
        domain = "PROCESS",
        text = text,
        options = options,
        correctIndex = 0,
        explanation = "explanation",
        bankVersion = 1,
    )

    private fun attempt(
        id: String,
        answeredAt: Long,
        isCorrect: Boolean,
    ) = AttemptEntity(
        id = id,
        questionId = "q1",
        sessionId = null,
        domain = "PROCESS",
        mode = "FREE",
        selectedIndex = 0,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
        updatedAt = answeredAt,
        syncState = "LOCAL_ONLY",
    )

    private fun session(
        id: String,
        status: String = "IN_PROGRESS",
        updatedAt: Long = 1L,
    ) = QuizSessionEntity(
        id = id,
        certificationId = "pmp",
        type = "SHORT_10",
        status = status,
        configJson = "{}",
        timeLimitMillis = null,
        elapsedMillis = 0L,
        currentIndex = 0,
        scorePercent = null,
        passed = null,
        createdAt = 1L,
        completedAt = null,
        updatedAt = updatedAt,
        syncState = "LOCAL_ONLY",
    )

    private fun sessionQuestion(
        sessionId: String,
        orderIndex: Int,
        questionId: String,
    ) = SessionQuestionEntity(
        sessionId = sessionId,
        orderIndex = orderIndex,
        questionId = questionId,
        selectedIndex = null,
        isCorrect = null,
    )
}
