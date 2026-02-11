package com.example.tasalicool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AboutScreen(navController: NavHostController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0E3B2E),
                        Color(0xFF0A2A21)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "🃏 tasalicool",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "حول التطبيق",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "tasalicool هو تطبيق ألعاب ورق عربية يقدم تجربة احترافية مدعومة بذكاء اصطناعي. " +
                        "يوفر التطبيق أيضاً إمكانية اللعب الجماعي عبر الشبكة المحلية لتجربة تنافسية حقيقية مع الأصدقاء.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            /* ================= DEVELOPER CARD ================= */

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B5E20)
                ),
                shape = MaterialTheme.shapes.large
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "المطور",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Mr million",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "klosemiroslave40@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE0E0E0),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            /* ================= APP FEATURES ================= */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "ميزات التطبيق",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text =
                        "• اللعب الفردي ضد ذكاء اصطناعي متقدم\n" +
                        "• اللعب الجماعي عبر الشبكة المحلية (Wi-Fi Local Multiplayer)\n" +
                        "• دعم حتى 4 لاعبين على نفس الشبكة\n" +
                        "• نظام حفظ واستكمال اللعبة\n\n" +
                        "اللعب الجماعي عبر Wi-Fi Local يعتمد على Socket Server داخل الشبكة المحلية، " +
                        "مما يسمح للأجهزة المتصلة بنفس الراوتر بالتواصل المباشر دون الحاجة إلى إنترنت خارجي. " +
                        "هذا يوفر سرعة عالية واستقرار ممتاز أثناء اللعب.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            /* ================= PRIVACY POLICY ================= */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "سياسة الخصوصية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text =
                        "نحن نحترم خصوصية المستخدمين.\n\n" +
                        "تطبيق tasalicool لا يقوم بجمع أو تخزين أو مشاركة أي بيانات شخصية.\n\n" +
                        "اللعب الجماعي عبر الشبكة المحلية يتم فقط داخل نفس الشبكة (Local Network) " +
                        "ولا يتم إرسال أي بيانات إلى خوادم خارجية.\n\n" +
                        "لا نقوم ببيع أو مشاركة أي بيانات مع أطراف خارجية.\n\n" +
                        "في حال إضافة ميزات مستقبلية مثل تسجيل الدخول أو الإعلانات " +
                        "سيتم تحديث سياسة الخصوصية بما يتوافق مع سياسات Google Play لعام 2026.\n\n" +
                        "للاستفسارات يمكن التواصل عبر البريد الإلكتروني أعلاه.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("العودة للرئيسية")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Developed by Mr million",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "© 2026 All Rights Reserved",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
