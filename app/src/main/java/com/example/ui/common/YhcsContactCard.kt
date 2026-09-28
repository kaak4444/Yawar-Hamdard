package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.YawarBlue

data class YhcsSupportContact(val key: String, val label: String, val phone: String)

val YhcsSupportContacts = listOf(
    YhcsSupportContact("YHCS1", "YHCS 1", "+93 707 438 303"),
    YhcsSupportContact("YHCS2", "YHCS 2", "+93 792 471 179")
)

@Composable
fun YhcsContactCard(
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("YHCS contact center", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Open either desk in the app to message or place an internet call.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            YhcsSupportContacts.forEach { contact ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaleBlue.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(contact.label, fontWeight = FontWeight.Bold, color = YawarBlue)
                        Text(contact.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(onClick = { onMessage(contact.key) }) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Message")
                    }
                }
            }
        }
    }
}
