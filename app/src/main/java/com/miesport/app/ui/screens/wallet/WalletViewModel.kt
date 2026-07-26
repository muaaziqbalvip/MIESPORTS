package com.miesport.app.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.UserProfile
import com.miesport.app.data.model.WalletTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val uid get() = auth.currentUser?.uid ?: ""

    val user: StateFlow<UserProfile?> = repo.observeUser(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<WalletTransaction>> = repo.observeUserTransactions(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDeposit(amount: Double, method: String) {
        viewModelScope.launch {
            repo.requestTransaction(
                WalletTransaction(userId = uid, type = "DEPOSIT", amount = amount, method = method, status = "PENDING")
            )
        }
    }

    fun requestWithdraw(amount: Double, method: String) {
        viewModelScope.launch {
            repo.requestTransaction(
                WalletTransaction(userId = uid, type = "WITHDRAW", amount = amount, method = method, status = "PENDING")
            )
        }
    }
}
