package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun LoginRegisterScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("eproject_prefs", Context.MODE_PRIVATE) }

    var isSignUpMode by remember { mutableStateOf(false) }

    var rememberMe by remember { mutableStateOf(sharedPrefs.getBoolean("remember_me", false)) }
    var email by remember { mutableStateOf(if (rememberMe) sharedPrefs.getString("saved_email", "") ?: "" else "") }
    var password by remember { mutableStateOf(if (rememberMe) sharedPrefs.getString("saved_password", "") ?: "" else "") }
    var confirmPassword by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") } // "user" or "admin"

    var passwordVisible by remember { mutableStateOf(false) }

    val darkTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF1E293B),
        unfocusedTextColor = Color(0xFF1E293B),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = Color(0xFF0D5DA3),
        unfocusedLabelColor = Color(0xFF64748B),
        focusedBorderColor = Color(0xFF0D5DA3),
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLeadingIconColor = Color(0xFF0D5DA3),
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedTrailingIconColor = Color(0xFF0D5DA3),
        unfocusedTrailingIconColor = Color(0xFF64748B)
    )

    val loginError by viewModel.loginError.collectAsState()
    val signupSuccess by viewModel.signupSuccess.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE6F0FA)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header / App Logo
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.img_app_logo),
                contentDescription = "Logo eProject",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Aplikasi Jalan dan Jembatan",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 0.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Card Form
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUpMode) "Daftar Akun Baru" else "Masuk ke Akun Anda",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Toggle (Sign In / Sign Up tabs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (!isSignUpMode) Color.White else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { isSignUpMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Masuk",
                                fontWeight = FontWeight.Bold,
                                color = if (!isSignUpMode) Color(0xFF0D5DA3) else Color(0xFF64748B)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSignUpMode) Color.White else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { isSignUpMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Daftar",
                                fontWeight = FontWeight.Bold,
                                color = if (isSignUpMode) Color(0xFF0D5DA3) else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (signupSuccess != null) {
                        Text(
                            text = signupSuccess ?: "",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Package Name Input for Sign Up
                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = packageName,
                                onValueChange = { packageName = it },
                                label = { Text("Nama Paket Pekerjaan") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = darkTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("package_name_input")
                            )

                            // Role Selection
                            Text(
                                text = "Daftar sebagai:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedRole = "user" }
                                ) {
                                    RadioButton(
                                        selected = selectedRole == "user",
                                        onClick = { selectedRole = "user" }
                                    )
                                    Text("Pengguna / User")
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedRole = "admin" }
                                ) {
                                    RadioButton(
                                        selected = selectedRole == "admin",
                                        onClick = { selectedRole = "admin" }
                                    )
                                    Text("Admin")
                                }
                            }
                        }
                    }

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = darkTextFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("email_input")
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = darkTextFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("password_input")
                    )

                    // Ingat Akun
                    AnimatedVisibility(
                        visible = !isSignUpMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ingat Akun",
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            )
                        }
                    }

                    // Confirm Password for Sign Up
                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Kata Sandi") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            colors = darkTextFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("confirm_password_input")
                        )
                    }

                    // Action Button
                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                if (password != confirmPassword) {
                                    viewModel.loginError.value = "Konfirmasi kata sandi tidak cocok."
                                } else {
                                    viewModel.signUp(email, password, selectedRole, packageName)
                                }
                            } else {
                                if (rememberMe) {
                                    sharedPrefs.edit()
                                        .putBoolean("remember_me", true)
                                        .putString("saved_email", email)
                                        .putString("saved_password", password)
                                        .apply()
                                } else {
                                    sharedPrefs.edit()
                                        .putBoolean("remember_me", false)
                                        .putString("saved_email", "")
                                        .putString("saved_password", "")
                                        .apply()
                                }
                                viewModel.login(email, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "DAFTAR SEKARANG" else "MASUK",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notice / Super admin info
                    Text(
                        text = "Pendaftaran akun baru memerlukan persetujuan oleh eproject.admin@gmail.com.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "eProject by Dendy Sofian ©2024",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0D5DA3)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
