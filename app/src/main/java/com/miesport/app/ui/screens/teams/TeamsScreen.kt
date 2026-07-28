package com.miesport.app.ui.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miesport.app.ui.components.GlassCard
import com.miesport.app.ui.screens.login.miTextFieldColors
import com.miesport.app.ui.theme.*

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onOpenTeamChat: (String) -> Unit = {}
) {
    val team by viewModel.myTeam.collectAsState()
    var teamNameInput by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(
                "Teams",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(20.dp)
            )

            if (team == null) {
                GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = NeonGreen)
                        Spacer(Modifier.height(8.dp))
                        Text("Create a Team", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = teamNameInput,
                            onValueChange = { teamNameInput = it },
                            label = { Text("Team Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = miTextFieldColors()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.createTeam(teamNameInput) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Create Team", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Join a Team", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { joinCodeInput = it },
                            label = { Text("Team ID / Invite Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = miTextFieldColors()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.joinTeam(joinCodeInput) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                        ) {
                            Text("Join Team", color = TextPrimary)
                        }
                    }
                }
            } else {
                val t = team!!
                GlassCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(t.name, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
                        Text("Team ID: ${t.id}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GlassCard { Column(Modifier.padding(12.dp)) {
                                Text("${t.wins}", color = NeonGreen, fontWeight = FontWeight.Bold)
                                Text("Wins", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            } }
                            GlassCard { Column(Modifier.padding(12.dp)) {
                                Text("Rs.${t.totalEarnings.toInt()}", color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text("Earnings", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            } }
                            GlassCard { Column(Modifier.padding(12.dp)) {
                                Text("${t.members.size}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Members", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            } }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onOpenTeamChat(t.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Open Team Chat", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}
