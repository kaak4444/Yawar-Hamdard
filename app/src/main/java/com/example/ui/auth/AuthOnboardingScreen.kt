package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.SlateSoft

@Composable
fun AuthOnboardingScreen(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    emailVerificationPending: Boolean,
    passwordResetPending: Boolean,
    onVerifyEmailCode: (String, String) -> Unit,
    onResendEmailCode: (String) -> Unit,
    onFinishPasswordReset: (String, String, String) -> Unit,
    onCancelCodeFlow: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    noticeMessage: String?,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isCreateAccount by remember { mutableStateOf(false) }
    var emailCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val isCodeFlow = emailVerificationPending || passwordResetPending

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language selector toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ClinicalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure MoPH Gateway",
                        color = Slate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Language pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = lang == currentLanguage
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(if (isSelected) PaleBlue else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) YawarBlue else BorderColor,
                                    MaterialTheme.shapes.small
                                )
                                .clickable { onLanguageSelected(lang) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ENGLISH -> "EN"
                                    AppLanguage.DARI -> "دری"
                                    AppLanguage.PASHTO -> "پښتو"
                                },
                                color = if (isSelected) YawarBlue else Ink,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compact white brand mark on a solid blue cover panel.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(colorResource(id = R.color.yawar_blue)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Yawar Hamdard Health Consulting Services",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Portal Welcome Title
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> if (isCreateAccount) "Create your Yawar Hamdard account" else "Sign In to Yawar Hamdard"
                    AppLanguage.DARI -> "ورود به خدمات صحی یاور همدرد"
                    AppLanguage.PASHTO -> "یاور همدرد روغتیایی خدمتونو ته ننوتل"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy,
                    fontSize = 21.sp
                )
            )

            Text(
                text = "Health Consulting Services • Afghanistan",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Account privileges are assigned by Firebase Admin SDK custom claims.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = PaleBlue),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = YawarBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Patient account • staff access is granted by an administrator", color = YawarNavy, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Login Credentials Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Role badge indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                emailVerificationPending -> "Verify your email address"
                                passwordResetPending -> "Reset your password"
                                isCreateAccount -> "Create a Patient Account"
                                else -> "Patient Portal Access"
                            },
                            fontWeight = FontWeight.Bold,
                            color = YawarNavy,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(PaleGreen)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    emailVerificationPending -> "VERIFY EMAIL"
                                    passwordResetPending -> "RESET CODE"
                                    else -> "EMAIL SIGN-IN"
                                },
                                color = ClinicalGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hostinger-backed account email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = YawarNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        enabled = !isLoading && !isCodeFlow,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { if (isCreateAccount) onSignUp(email, password) else onSignIn(email, password) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YawarBlue,
                            unfocusedBorderColor = BorderColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isCodeFlow) {
                        Text(
                            text = if (emailVerificationPending)
                                "Enter the six-digit code sent from no-reply@yawarconsulting.com."
                            else
                                "Enter the six-digit code sent to your email address.",
                            color = Slate,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        )
                        OutlinedTextField(
                            value = emailCode,
                            onValueChange = { value -> emailCode = value.filter(Char::isDigit).take(6) },
                            label = { Text("Six-digit email code") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = YawarNavy, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YawarBlue,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        if (passwordResetPending) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New password (at least 12 characters)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = YawarNavy, modifier = Modifier.size(18.dp))
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = YawarBlue,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (at least 12 characters)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = YawarNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Slate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YawarBlue,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (errorMessage != null) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (noticeMessage != null) {
                        Text(noticeMessage, color = ClinicalGreen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Primary Sign In Button
                    Button(
                        onClick = {
                            when {
                                emailVerificationPending -> onVerifyEmailCode(email, emailCode)
                                passwordResetPending -> onFinishPasswordReset(email, emailCode, newPassword)
                                isCreateAccount -> onSignUp(email, password)
                                else -> onSignIn(email, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YawarNavy
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting securely…", fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = when {
                                    emailVerificationPending -> "Verify email"
                                    passwordResetPending -> "Save new password"
                                    currentLanguage == AppLanguage.ENGLISH -> if (isCreateAccount) "Create Account" else "Sign In to Portal"
                                    currentLanguage == AppLanguage.DARI -> "ورود به پورتال"
                                    else -> "پورتال ته ننوتل"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    if (!isCreateAccount && !isCodeFlow) {
                        Text(
                            text = "Forgot password?",
                            color = YawarBlue,
                            modifier = Modifier.align(Alignment.End).clickable(enabled = !isLoading) { onPasswordReset(email) }.padding(top = 12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isCodeFlow) {
                        Text(
                            text = if (emailVerificationPending) "Resend verification code" else "Resend password reset code",
                            color = YawarBlue,
                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable(enabled = !isLoading) {
                                if (emailVerificationPending) onResendEmailCode(email) else onPasswordReset(email)
                            }.padding(top = 12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (isCodeFlow) "Back to sign in" else if (isCreateAccount) "Already have an account? Sign in" else "New here? Create an account",
                        color = YawarBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally).clickable(enabled = !isLoading) {
                            if (isCodeFlow) {
                                onCancelCodeFlow()
                                emailCode = ""
                                newPassword = ""
                                isCreateAccount = false
                            } else {
                                isCreateAccount = !isCreateAccount
                            }
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Emergency Support Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(PaleBlue)
                    .border(1.dp, BorderColor, MaterialTheme.shapes.small)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PaleBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = YawarBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24/7 Patient Emergency Hotline",
                            fontWeight = FontWeight.Bold,
                            color = YawarNavy,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "+93 707 438 303 • callcenter@yawarconsulting.com",
                            color = Slate,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
