package dev.ide.agent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.ide.agent.AgentSessionMeta
import dev.ide.ui.backend.IdeBackend
import dev.ide.ui.icons.CaIcons
import dev.ide.ui.theme.Ca

/**
 * A slide-in panel listing the user's persisted agent sessions. Observes [AgentService.sessions] and offers
 * load / delete / rename. Shown over the chat drawer when the user taps the history button.
 */
@Composable
fun SessionListPanel(
    backend: IdeBackend,
    onClose: () -> Unit,
) {
    val sessions by backend.agent.sessions.collectAsState()
    val currentId = backend.agent.currentSessionId()

    Column(
        Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(Ca.colors.surface)
            .padding(vertical = 12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "History",
                color = Ca.colors.textPrimary,
                style = Ca.type.subhead,
                modifier = Modifier.weight(1f),
            )
            Icon(
                CaIcons.close, "Close history",
                Modifier.size(18.dp).clip(RoundedCornerShape(Ca.radius.pill)).clickable(onClick = onClose),
                tint = Ca.colors.textSecondary,
            )
        }
        Hairline()

        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No history yet.\nStart a conversation to see it here.",
                    color = Ca.colors.textTertiary,
                    style = Ca.type.body,
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        isActive = session.id == currentId,
                        onClick = {
                            backend.agent.loadSession(session.id)
                            onClose()
                        },
                        onDelete = { backend.agent.deleteSession(session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: AgentSessionMeta,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val bg = if (isActive) Ca.colors.accentSoft else androidx.compose.ui.graphics.Color.Transparent
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(Ca.radius.card))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            CaIcons.docText, null,
            Modifier.size(16.dp),
            tint = if (isActive) Ca.colors.accent else Ca.colors.textSecondary,
        )
        Column(Modifier.weight(1f)) {
            Text(
                session.title,
                color = Ca.colors.textPrimary,
                style = Ca.type.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatRelativeTime(session.updatedAt),
                color = Ca.colors.textTertiary,
                style = Ca.type.caption2,
            )
        }
        Icon(
            CaIcons.close, "Delete session",
            Modifier.size(14.dp).clip(RoundedCornerShape(Ca.radius.pill)).clickable(onClick = onDelete),
            tint = Ca.colors.textTertiary,
        )
    }
}

/** "Just now", "5m ago", "3h ago", "Mar 24". */
private fun formatRelativeTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val delta = now - epochMs
    val minutes = delta / 60_000
    val hours = delta / 3_600_000
    val days = delta / 86_400_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
            val month = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${month[cal.get(java.util.Calendar.MONTH)]} ${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
        }
    }
}
