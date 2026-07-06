package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/** UI model for one option within a [QuestionCard]. */
data class AnswerOptionUi(
    val text: String,
    val state: OptionState,
)

/** Presents a question stem and its options. Stateless: option states + enablement come from the caller. */
@Composable
fun QuestionCard(
    questionText: String,
    options: List<AnswerOptionUi>,
    onOptionSelected: (Int) -> Unit,
    optionsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PmpSpacing.basePadding),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            Text(text = questionText, style = MaterialTheme.typography.headlineMedium)
            options.forEachIndexed { index, option ->
                AnswerOption(
                    text = option.text,
                    label = ('A' + index).toString(),
                    state = option.state,
                    enabled = optionsEnabled,
                    onClick = { onOptionSelected(index) },
                )
            }
        }
    }
}

private const val PREVIEW_STEM =
    "What should the project manager do first when two team members disagree?"

@Preview(name = "Unanswered")
@Composable
private fun QuestionCardUnansweredPreview() {
    PmpTheme {
        QuestionCard(
            questionText = PREVIEW_STEM,
            options = listOf(
                AnswerOptionUi("Escalate to the sponsor", OptionState.DEFAULT),
                AnswerOptionUi("Facilitate a collaborative resolution", OptionState.SELECTED),
                AnswerOptionUi("Pick the senior member's approach", OptionState.DEFAULT),
                AnswerOptionUi("Separate them onto different tasks", OptionState.DEFAULT),
            ),
            onOptionSelected = {},
            optionsEnabled = true,
        )
    }
}

@Preview(name = "Graded")
@Composable
private fun QuestionCardGradedPreview() {
    PmpTheme {
        QuestionCard(
            questionText = PREVIEW_STEM,
            options = listOf(
                AnswerOptionUi("Escalate to the sponsor", OptionState.DEFAULT),
                AnswerOptionUi("Facilitate a collaborative resolution", OptionState.CORRECT),
                AnswerOptionUi("Pick the senior member's approach", OptionState.INCORRECT),
                AnswerOptionUi("Separate them onto different tasks", OptionState.DEFAULT),
            ),
            onOptionSelected = {},
            optionsEnabled = false,
        )
    }
}
