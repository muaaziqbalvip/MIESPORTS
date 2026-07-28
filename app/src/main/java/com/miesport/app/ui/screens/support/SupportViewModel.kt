package com.miesport.app.ui.screens.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miesport.app.data.firebase.FirestoreRepository
import com.miesport.app.data.model.PaymentMethod
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SupportViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    val paymentMethods: StateFlow<List<PaymentMethod>> = repo.observePaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
