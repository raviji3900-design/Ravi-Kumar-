package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun AuthScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Admin, 1: Operator

    // Admin state
    var adminPassword by remember { mutableStateOf("") }
    var adminPasswordVisible by remember { mutableStateOf(false) }
    var adminError by remember { mutableStateOf<String?>(null) }

    // Operator state
    var opUsername by remember { mutableStateOf("") }
    var opPassword by remember { mutableStateOf("") }
    var opPasswordVisible by remember { mutableStateOf(false) }
    var opError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("app_title", language),
                subtitle = AppStrings.t("app_subtitle", language),
                currentLanguage = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                showBackButton = false,
                showLogout = false
            )
        },
        containerColor = NeutralBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App Emblem / Icon Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.size(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_icon),
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = AppStrings.t("app_title", language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (language == AppLanguage.HI) "सुरक्षित हज़िरी प्रबंधन प्रणाली (एडमिन और ऑपरेटर)" else "Secure Loading Works Attendance System",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector: Admin vs Operator
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceWhite,
                    contentColor = PrimaryBlueLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryBlueLight,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; adminError = null; opError = null },
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .testTag("admin_tab_btn"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Admin",
                                    tint = if (selectedTab == 0) PrimaryBlueLight else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = AppStrings.t("admin_login", language),
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) PrimaryBlueLight else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; adminError = null; opError = null },
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .testTag("operator_tab_btn"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = "Operator",
                                    tint = if (selectedTab == 1) PrimaryBlueLight else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = AppStrings.t("operator_login", language),
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) PrimaryBlueLight else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    if (selectedTab == 0) {
                        // === ADMIN LOGIN FORM ===
                        Text(
                            text = AppStrings.t("enter_password", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it; adminError = null },
                            placeholder = { Text("admin123", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = PrimaryBlueLight)
                            },
                            trailingIcon = {
                                IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                    Icon(
                                        imageVector = if (adminPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_input")
                        )

                        if (adminError != null) {
                            Text(
                                text = adminError ?: "",
                                color = StatusAbsentRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.loginAdmin(
                                    password = adminPassword.trim(),
                                    onSuccess = {},
                                    onError = { adminError = it }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_signin_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                        ) {
                            Text(
                                text = AppStrings.t("login_button", language) + " (Admin)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick demo helper button for convenient testing
                        OutlinedButton(
                            onClick = { adminPassword = "admin123"; adminError = null },
                            modifier = Modifier.fillMaxWidth().testTag("quick_fill_admin_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Auto Fill Demo Admin (admin123)",
                                fontSize = 12.sp,
                                color = PrimaryBlueLight
                            )
                        }

                    } else {
                        // === OPERATOR LOGIN FORM ===
                        Text(
                            text = AppStrings.t("username", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = opUsername,
                            onValueChange = { opUsername = it; opError = null },
                            placeholder = { Text("e.g. operator1", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Operator ID", tint = PrimaryBlueLight)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("operator_username_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = AppStrings.t("password", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = opPassword,
                            onValueChange = { opPassword = it; opError = null },
                            placeholder = { Text("e.g. 123", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = PrimaryBlueLight)
                            },
                            trailingIcon = {
                                IconButton(onClick = { opPasswordVisible = !opPasswordVisible }) {
                                    Icon(
                                        imageVector = if (opPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (opPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("operator_password_input")
                        )

                        if (opError != null) {
                            Text(
                                text = opError ?: "",
                                color = StatusAbsentRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.loginOperator(
                                    username = opUsername.trim(),
                                    pass = opPassword.trim(),
                                    onSuccess = {},
                                    onError = { opError = it }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("operator_signin_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                        ) {
                            Text(
                                text = AppStrings.t("login_button", language) + " (Operator)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick demo buttons for Operators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    opUsername = "operator1"
                                    opPassword = "123"
                                    opError = null
                                },
                                modifier = Modifier.weight(1f).testTag("quick_op1_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Fill Op 1 (Rajesh)", fontSize = 11.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    opUsername = "operator2"
                                    opPassword = "123"
                                    opError = null
                                },
                                modifier = Modifier.weight(1f).testTag("quick_op2_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Fill Op 2 (Manoj)", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security & Offline feature note
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = if (language == AppLanguage.HI) "सुरक्षित रोल-आधारित एक्सेस" else "Role-Based Security & Offline Ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = if (language == AppLanguage.HI) "ऑपरेटर केवल अपने मजदूरों की हज़िरी भर सकते हैं। एडमिन सभी सेटिंग्स नियंत्रित करते हैं।"
                            else "Operators only access their assigned workers. Admins have full access to management and reports.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
