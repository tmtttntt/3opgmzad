package com.opengate.app.ui.screens.auction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** نافذة بدء مزاد جديد — نفس منطق التحقق السابق دون تغيير، تتلوّن تلقائياً بثيم المزادات. */
@Composable
internal fun CreateAuctionDialog(
    creating: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, startPrice: Double, minInc: Double, durationHours: Long, city: String?, phone: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startingPrice by remember { mutableStateOf("") }
    var minIncrement by remember { mutableStateOf("1000") }
    var durationHours by remember { mutableStateOf(24L) }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!creating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("بدء مزاد مباشر جديد", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("اسم السلعة المعروضة للمزاد *") },
                    placeholder = { Text("مثال: ساعة رولكس، سيارة إلنترا 2021...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startingPrice,
                        onValueChange = { startingPrice = it; validationError = null },
                        label = { Text("السعر الافتتاحي (ر.ي) *") },
                        placeholder = { Text("10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minIncrement,
                        onValueChange = { minIncrement = it },
                        label = { Text("أقل زيادة (ر.ي)") },
                        placeholder = { Text("1000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف للتواصل") },
                    placeholder = { Text("77xxxxxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("المدينة") },
                    placeholder = { Text("صنعاء، عدن...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("مدة المزاد المباشر:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1L to "1 ساعة",
                        6L to "6 ساعات",
                        24L to "24 ساعة",
                        72L to "3 أيام"
                    ).forEach { (h, label) ->
                        FilterChip(
                            selected = durationHours == h,
                            onClick = { durationHours = h },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                validationError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sPrice = startingPrice.toDoubleOrNull()
                    val mInc = minIncrement.toDoubleOrNull() ?: 1000.0
                    if (title.isBlank()) {
                        validationError = "يرجى كتابة اسم السلعة"
                        return@Button
                    }
                    if (sPrice == null || sPrice <= 0) {
                        validationError = "يرجى كتابة سعر افتتاحي صحيح"
                        return@Button
                    }
                    onSubmit(title.trim(), sPrice, mInc, durationHours, city.trim().ifBlank { null }, phone.trim().ifBlank { null })
                },
                enabled = !creating
            ) {
                if (creating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("إطلاق المزاد 🚀", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating) {
                Text("إلغاء")
            }
        }
    )
}
