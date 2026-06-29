package com.safarsakha.presentation.screens.profile.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarsakha.core.utils.Resource
import com.safarsakha.data.remote.firebase.FirebaseBookingDataSource
import com.safarsakha.data.remote.firebase.FirebaseTransactionDataSource
import com.safarsakha.data.repository.impl.BookingRepositoryImpl
import com.safarsakha.data.repository.impl.TransactionRepositoryImpl
import com.safarsakha.domain.model.Booking
import com.safarsakha.domain.model.BookingStatus
import com.safarsakha.domain.model.PaymentStatus
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.model.Transaction
import com.safarsakha.domain.usecase.booking.CreateBookingUseCase
import com.safarsakha.domain.usecase.transaction.CreateTransactionUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.reflect.KClass

// ── Design tokens ───────────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val SuccessColor = Color(0xFF16A34A) // Premium Green Color Token

private enum class BookingStep { DATE_SELECTION, PAYMENT, PROCESSING, RESULT }

private data class BookingFlowState(
    val step: BookingStep = BookingStep.DATE_SELECTION,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val isProcessing: Boolean = false,
    val paymentSuccess: Boolean? = null,
    val errorMessage: String? = null,
    val createdBookingId: String? = null
)

private class BookingFlowViewModel(
    private val tourPackage: TourPackage,
    private val createBookingUseCase: CreateBookingUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _state = mutableStateOf(BookingFlowState())
    val state: State<BookingFlowState> get() = _state

    fun setStartDate(d: LocalDate) { _state.value = _state.value.copy(startDate = d, errorMessage = null) }
    fun setEndDate(d: LocalDate) { _state.value = _state.value.copy(endDate = d, errorMessage = null) }

    fun proceedToPayment() {
        val s = _state.value.startDate
        val e = _state.value.endDate
        if (s == null || e == null) { _state.value = _state.value.copy(errorMessage = "Please select both start and end dates"); return }
        if (e < s) { _state.value = _state.value.copy(errorMessage = "End date must be after start date"); return }
        _state.value = _state.value.copy(step = BookingStep.PAYMENT, errorMessage = null)
    }

    fun updateCard(holderName: String? = null, number: String? = null, expiry: String? = null, cvv: String? = null) {
        _state.value = _state.value.copy(
            cardHolderName = holderName ?: _state.value.cardHolderName,
            cardNumber = number ?: _state.value.cardNumber,
            expiryDate = expiry ?: _state.value.expiryDate,
            cvv = cvv ?: _state.value.cvv,
            errorMessage = null
        )
    }

    fun processPayment(scope: kotlinx.coroutines.CoroutineScope) {
        val s = _state.value
        if (s.cardHolderName.isBlank()) { _state.value = s.copy(errorMessage = "Card holder name is required"); return }
        val cleanCard = s.cardNumber.replace(" ", "").replace("-", "")
        if (cleanCard.length < 13 || cleanCard.length > 19 || !cleanCard.all { it.isDigit() }) {
            _state.value = s.copy(errorMessage = "Enter a valid card number (13–19 digits)"); return
        }
        if (!isValidExpiry(s.expiryDate)) { _state.value = s.copy(errorMessage = "Enter a valid expiry date (MM/YY)"); return }
        if (s.cvv.length < 3 || !s.cvv.all { it.isDigit() }) { _state.value = s.copy(errorMessage = "CVV must be 3–4 digits"); return }

        _state.value = s.copy(step = BookingStep.PROCESSING, isProcessing = true, errorMessage = null)

        scope.launch {
            delay(2000)

            val success = (abs(Clock.System.now().toEpochMilliseconds()) % 10) < 7

            val userId = Firebase.auth.currentUser?.uid ?: ""
            val userName = Firebase.auth.currentUser?.displayName ?: "User"
            val startDate = _state.value.startDate!!
            val endDate = _state.value.endDate!!
            val nights = (endDate.toEpochDays() - startDate.toEpochDays()).coerceAtLeast(1)
            val totalAmount = tourPackage.price * nights

            val transaction = Transaction(
                bookingId = "",
                userId = userId,
                amount = totalAmount,
                paymentMethod = "Card",
                paymentStatus = if (success) PaymentStatus.SUCCESS else PaymentStatus.FAILED,
                transactionDate = Clock.System.now()
            )

            if (success) {
                val booking = Booking(
                    userId = userId,
                    userName = userName,
                    packageId = tourPackage.id,
                    packageName = tourPackage.title,
                    packagePrice = tourPackage.price,
                    startDate = startDate,
                    endDate = endDate,
                    bookingStatus = BookingStatus.UPCOMING,
                    paymentStatus = PaymentStatus.SUCCESS,
                    totalAmount = totalAmount
                )
                val bookingResult = createBookingUseCase(booking)
                val createdBookingId = when (bookingResult) {
                    is Resource.Success -> bookingResult.data?.bookingId ?: ""
                    else -> ""
                }
                createTransactionUseCase(transaction.copy(bookingId = createdBookingId))
                _state.value = _state.value.copy(
                    isProcessing = false,
                    paymentSuccess = true,
                    step = BookingStep.RESULT,
                    createdBookingId = createdBookingId
                )
            } else {
                createTransactionUseCase(transaction)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    paymentSuccess = false,
                    step = BookingStep.RESULT
                )
            }
        }
    }

    fun goBack() {
        _state.value = when (_state.value.step) {
            BookingStep.PAYMENT -> _state.value.copy(step = BookingStep.DATE_SELECTION)
            else -> _state.value
        }
    }

    private fun isValidExpiry(expiry: String): Boolean {
        val parts = expiry.split("/")
        if (parts.size != 2) return false
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        if (month < 1 || month > 12) return false
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val fullYear = if (year < 100) 2000 + year else year
        return fullYear > now.year || (fullYear == now.year && month >= now.monthNumber)
    }
}

@Composable
fun BookingScreen(
    tourPackage: TourPackage,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    onPaymentFailed: () -> Unit
) {
    val bookingRepository = remember { BookingRepositoryImpl(FirebaseBookingDataSource()) }
    val transactionRepository = remember { TransactionRepositoryImpl(FirebaseTransactionDataSource()) }

    val viewModel: BookingFlowViewModel = viewModel(
        key = "booking_${tourPackage.id}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return BookingFlowViewModel(
                    tourPackage = tourPackage,
                    createBookingUseCase = CreateBookingUseCase(bookingRepository),
                    createTransactionUseCase = CreateTransactionUseCase(transactionRepository)
                ) as T
            }
        }
    )

    val state by viewModel.state
    val scope = rememberCoroutineScope()

    when (state.step) {
        BookingStep.DATE_SELECTION -> DateSelectionStep(
            tourPackage = tourPackage,
            startDate = state.startDate,
            endDate = state.endDate,
            errorMessage = state.errorMessage,
            onStartDateSet = { viewModel.setStartDate(it) },
            onEndDateSet = { viewModel.setEndDate(it) },
            onNext = { viewModel.proceedToPayment() },
            onBack = onNavigateBack
        )
        BookingStep.PAYMENT -> PaymentStep(
            tourPackage = tourPackage,
            startDate = state.startDate!!,
            endDate = state.endDate!!,
            cardHolderName = state.cardHolderName,
            cardNumber = state.cardNumber,
            expiryDate = state.expiryDate,
            cvv = state.cvv,
            errorMessage = state.errorMessage,
            onCardUpdate = { holderName, number, expiry, cvv ->
                viewModel.updateCard(holderName, number, expiry, cvv)
            },
            onPay = { viewModel.processPayment(scope) },
            onBack = { viewModel.goBack() }
        )
        BookingStep.PROCESSING -> ProcessingStep()
        BookingStep.RESULT -> ResultStep(
            success = state.paymentSuccess == true,
            onSuccessAction = onBookingSuccess,
            onFailedAction = onPaymentFailed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectionStep(
    tourPackage: TourPackage,
    startDate: LocalDate?,
    endDate: LocalDate?,
    errorMessage: String?,
    onStartDateSet: (LocalDate) -> Unit,
    onEndDateSet: (LocalDate) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val nights = if (startDate != null && endDate != null && endDate >= startDate)
        (endDate.toEpochDays() - startDate.toEpochDays()).toInt() else 0
    val totalAmount = if (nights > 0) tourPackage.price * nights else tourPackage.price

    if (showStartPicker) {
        SimpleDatePickerDialog(
            title = "Select Start Date",
            onDateSelected = { date -> onStartDateSet(date); showStartPicker = false },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        SimpleDatePickerDialog(
            title = "Select End Date",
            onDateSelected = { date -> onEndDateSet(date); showEndPicker = false },
            onDismiss = { showEndPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Book Tour",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Select your travel dates",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BgColor,
                border = borderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📦 Package Summary", fontWeight = FontWeight.Bold, color = NavyColor, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tourPackage.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = NavyColor, modifier = Modifier.weight(1f))
                        Text("₹${tourPackage.price}/night", fontWeight = FontWeight.Bold, color = SuccessColor, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("📍 ${tourPackage.location}", fontSize = 13.sp, color = SlateColor)
                    Text("⏱️ ${tourPackage.duration}", fontSize = 13.sp, color = SlateColor)
                }
            }

            Text("Select Travel Dates", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyColor)

            DatePickerField(label = "Start Date", date = startDate, placeholder = "Tap to select start date", onClick = { showStartPicker = true })
            DatePickerField(label = "End Date", date = endDate, placeholder = "Tap to select end date", onClick = { showEndPicker = true })

            if (nights > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightBgColor,
                    border = borderStroke(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Booking Summary", fontWeight = FontWeight.Bold, color = NavyColor, fontSize = 14.sp)
                        HorizontalDivider(color = BorderColor)
                        SummaryRow("Duration", "$nights night${if (nights != 1) "s" else ""}")
                        SummaryRow("Price per night", "₹${tourPackage.price}")
                        HorizontalDivider(color = BorderColor)
                        SummaryRow("Total Amount", "₹$totalAmount", bold = true, isHighlightGreen = true)
                    }
                }
            }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
            ) {
                Text("Continue to Payment →", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentStep(
    tourPackage: TourPackage,
    startDate: LocalDate,
    endDate: LocalDate,
    cardHolderName: String,
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    errorMessage: String?,
    onCardUpdate: (holderName: String?, number: String?, expiry: String?, cvv: String?) -> Unit,
    onPay: () -> Unit,
    onBack: () -> Unit
) {
    val nights = (endDate.toEpochDays() - startDate.toEpochDays()).coerceAtLeast(1).toInt()
    val totalAmount = tourPackage.price * nights

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payment",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.3f).sp
                        )
                        Text(
                            text = "Secure checkout",
                            fontSize = 12.sp,
                            color = SlateColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = BorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBgColor)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = BgColor, border = borderStroke(), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.Bold, color = NavyColor, fontSize = 14.sp)
                    HorizontalDivider(color = BorderColor)
                    SummaryRow("Package", tourPackage.title)
                    SummaryRow("Start Date", startDate.toString())
                    SummaryRow("End Date", endDate.toString())
                    SummaryRow("Nights", "$nights")
                    HorizontalDivider(color = BorderColor)
                    SummaryRow("Total", "₹$totalAmount", bold = true, isHighlightGreen = true)
                }
            }

            Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyColor)

            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFFBEB), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "🔒 Demo Mode — No real payment is processed",
                    fontSize = 12.sp, color = Color(0xFF92400E),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Surface(shape = RoundedCornerShape(12.dp), color = BgColor, border = borderStroke(), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = cardHolderName,
                        onValueChange = { onCardUpdate(it, null, null, null) },
                        label = { Text("Card Holder Name", color = SlateColor) },
                        placeholder = { Text("John Doe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = formatCardNumber(cardNumber),
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(16)
                            onCardUpdate(null, digits, null, null)
                        },
                        label = { Text("Card Number", color = SlateColor) },
                        placeholder = { Text("1234 5678 9012 3456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { raw ->
                                val digits = raw.filter { it.isDigit() }.take(4)
                                val formatted = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
                                onCardUpdate(null, null, formatted, null)
                            },
                            label = { Text("Expiry", color = SlateColor) },
                            placeholder = { Text("MM/YY") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { raw ->
                                val digits = raw.filter { it.isDigit() }.take(4)
                                onCardUpdate(null, null, null, digits)
                            },
                            label = { Text("CVV", color = SlateColor) },
                            placeholder = { Text("•••") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Button(
                onClick = onPay,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
            ) {
                Text("Pay ₹$totalAmount & Confirm Booking", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProcessingStep() {
    Box(modifier = Modifier.fillMaxSize().background(LightBgColor), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CircularProgressIndicator(color = SkyColor, strokeWidth = 3.dp, modifier = Modifier.size(56.dp))
            Text("Processing Payment...", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = NavyColor)
            Text("Please do not close this screen", fontSize = 13.sp, color = SlateColor)
        }
    }
}

@Composable
private fun ResultStep(success: Boolean, onSuccessAction: () -> Unit, onFailedAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(LightBgColor), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(20.dp), color = BgColor, border = borderStroke(), modifier = Modifier.padding(32.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(if (success) "✅" else "❌", fontSize = 56.sp)
                Text(
                    if (success) "Booking Confirmed!" else "Payment Failed",
                    fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    color = if (success) SuccessColor else Color.Red,
                    textAlign = TextAlign.Center
                )
                Text(
                    if (success) "Your booking has been saved. You can track it in My Bookings."
                    else "Your payment could not be processed. The failed attempt has been recorded in Transactions.",
                    fontSize = 14.sp, color = SlateColor, textAlign = TextAlign.Center, lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = if (success) onSuccessAction else onFailedAction,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (success) "View My Bookings" else "View Transactions", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SimpleDatePickerDialog(
    title: String,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedYear by remember { mutableStateOf(today.year) }
    var selectedMonth by remember { mutableStateOf(today.monthNumber) }
    var selectedDay by remember { mutableStateOf(today.dayOfMonth) }

    val daysInMonth = daysInMonth(selectedYear, selectedMonth)
    if (selectedDay > daysInMonth) selectedDay = daysInMonth

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = BgColor, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyColor)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        if (selectedMonth == 1) { selectedMonth = 12; selectedYear-- } else selectedMonth--
                    }) { Text("‹", fontSize = 20.sp, color = NavyColor) }
                    Text("${monthName(selectedMonth)} $selectedYear", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = NavyColor)
                    TextButton(onClick = {
                        if (selectedMonth == 12) { selectedMonth = 1; selectedYear++ } else selectedMonth++
                    }) { Text("›", fontSize = 20.sp, color = NavyColor) }
                }

                val firstDayOffset = firstDayOfWeek(selectedYear, selectedMonth)
                val weeks = ((daysInMonth + firstDayOffset + 6) / 7)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(day, fontSize = 11.sp, color = SlateColor, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        }
                    }
                    for (week in 0 until weeks) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (dow in 0 until 7) {
                                val dayNum = week * 7 + dow - firstDayOffset + 1
                                if (dayNum < 1 || dayNum > daysInMonth) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val isSelected = dayNum == selectedDay
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .background(if (isSelected) NavyColor else Color.Transparent, RoundedCornerShape(50))
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$dayNum", fontSize = 13.sp, color = if (isSelected) Color.White else NavyColor, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = SlateColor) }
                    Button(
                        onClick = { onDateSelected(LocalDate(selectedYear, selectedMonth, selectedDay)) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Select", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun DatePickerField(label: String, date: LocalDate?, placeholder: String, onClick: () -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SlateColor)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .background(BgColor, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📅 ", fontSize = 16.sp)
                Text(
                    date?.toString() ?: placeholder,
                    fontSize = 14.sp,
                    color = if (date != null) NavyColor else SlateColor
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, isHighlightGreen: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = SlateColor)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlightGreen) SuccessColor else NavyColor
        )
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, BorderColor.copy(alpha = 0.5f))

private fun formatCardNumber(digits: String): String = digits.chunked(4).joinToString(" ")

private fun daysInMonth(year: Int, month: Int): Int {
    val days30 = setOf(4, 6, 9, 11)
    return when {
        month in days30 -> 30
        month == 2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 31
    }
}

private fun firstDayOfWeek(year: Int, month: Int): Int {
    val y = if (month < 3) year - 1 else year
    val m = if (month < 3) month + 12 else month
    val k = y % 100
    val j = y / 100
    val h = (1 + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    return ((h + 5) % 7)
}

private fun monthName(month: Int) = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[month - 1]