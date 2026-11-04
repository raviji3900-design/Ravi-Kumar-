package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.Operator
import com.example.data.repository.AttendanceRepository
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class UserRole {
    NONE,
    ADMIN,
    OPERATOR
}

enum class AppScreen {
    AUTH,
    ADMIN_DASHBOARD,
    OPERATOR_DASHBOARD,
    MARK_ATTENDANCE,
    EMPLOYEE_MANAGEMENT,
    OPERATOR_MANAGEMENT,
    REPORTS,
    CALENDAR,
    BACKUP_SETTINGS
}

data class AttendanceSummary(
    val totalEmployees: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val halfDayCount: Int = 0,
    val leaveCount: Int = 0,
    val attendanceRate: Float = 0f
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AttendanceRepository(db.attendanceDao(), application)
    }

    // === Navigation & Session ===
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<AppScreen>()

    private val _userRole = MutableStateFlow(UserRole.NONE)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _currentOperator = MutableStateFlow<Operator?>(null)
    val currentOperator: StateFlow<Operator?> = _currentOperator.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.DUAL)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.removeAt(screenHistory.size - 1)
            _currentScreen.value = prev
            return true
        }
        if (_currentScreen.value != AppScreen.AUTH) {
            _currentScreen.value = if (_userRole.value == UserRole.ADMIN) AppScreen.ADMIN_DASHBOARD else AppScreen.OPERATOR_DASHBOARD
            return true
        }
        return false
    }

    fun logout() {
        _userRole.value = UserRole.NONE
        _currentOperator.value = null
        screenHistory.clear()
        _currentScreen.value = AppScreen.AUTH
    }

    // === Selected Date for Attendance ===
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // === Data Streams ===
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOperators: StateFlow<List<Operator>> = repository.allOperators.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendance.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered attendance for currently selected date
    val attendanceForSelectedDate: StateFlow<List<AttendanceRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Summary calculation for the selected date
    val todaySummary: StateFlow<AttendanceSummary> = combine(
        allEmployees,
        attendanceForSelectedDate
    ) { employees, records ->
        val total = employees.size
        var present = 0
        var absent = 0
        var halfDay = 0
        var leave = 0

        for (r in records) {
            when (AttendanceStatus.fromCode(r.status)) {
                AttendanceStatus.PRESENT -> present++
                AttendanceStatus.ABSENT -> absent++
                AttendanceStatus.HALF_DAY -> halfDay++
                AttendanceStatus.LEAVE -> leave++
            }
        }

        val rate = if (total > 0) {
            ((present + (halfDay * 0.5f)) / total.toFloat()) * 100f
        } else 0f

        AttendanceSummary(
            totalEmployees = total,
            presentCount = present,
            absentCount = absent,
            halfDayCount = halfDay,
            leaveCount = leave,
            attendanceRate = rate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttendanceSummary()
    )

    // === Authentication Logic ===
    fun loginAdmin(password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val ok = repository.verifyAdminPassword(password)
            if (ok) {
                _userRole.value = UserRole.ADMIN
                _currentOperator.value = null
                screenHistory.clear()
                _currentScreen.value = AppScreen.ADMIN_DASHBOARD
                onSuccess()
            } else {
                onError("Incorrect password / गलत पासवर्ड")
            }
        }
    }

    fun loginOperator(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val op = repository.authenticateOperator(username.trim(), pass)
            if (op != null) {
                _userRole.value = UserRole.OPERATOR
                _currentOperator.value = op
                screenHistory.clear()
                _currentScreen.value = AppScreen.OPERATOR_DASHBOARD
                onSuccess()
            } else {
                onError("Invalid operator username or password / अमान्य ऑपरेटर उपयोगकर्ता नाम या पासवर्ड")
            }
        }
    }

    fun changeAdminPassword(currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!repository.verifyAdminPassword(currentPass)) {
                onResult(false, "Current password incorrect / वर्तमान पासवर्ड गलत है")
                return@launch
            }
            if (newPass.length < 3) {
                onResult(false, "Password must be at least 3 characters / पासवर्ड कम से कम 3 अक्षरों का होना चाहिए")
                return@launch
            }
            repository.updateAdminPassword(newPass)
            onResult(true, "Password updated successfully / पासवर्ड सफलतापूर्वक बदला गया")
        }
    }

    // === Attendance Actions ===
    fun markAttendance(
        employeeId: String,
        employeeName: String,
        status: AttendanceStatus,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val role = if (_userRole.value == UserRole.ADMIN) "ADMIN" else "OPERATOR"
            val id = if (_userRole.value == UserRole.ADMIN) "ADMIN" else (_currentOperator.value?.id ?: "OP")
            val name = if (_userRole.value == UserRole.ADMIN) "Admin" else (_currentOperator.value?.name ?: "Operator")

            repository.markAttendance(
                employeeId = employeeId,
                employeeName = employeeName,
                status = status,
                date = _selectedDate.value,
                markedByRole = role,
                markedById = id,
                markedByName = name,
                notes = notes
            )
            showToast("Recorded: $employeeName -> ${status.labelEn} (${status.code})")
        }
    }

    fun markAllPresentForActiveList(employees: List<Employee>) {
        viewModelScope.launch {
            val role = if (_userRole.value == UserRole.ADMIN) "ADMIN" else "OPERATOR"
            val id = if (_userRole.value == UserRole.ADMIN) "ADMIN" else (_currentOperator.value?.id ?: "OP")
            val name = if (_userRole.value == UserRole.ADMIN) "Admin" else (_currentOperator.value?.name ?: "Operator")

            repository.markAllPresent(
                employees = employees,
                date = _selectedDate.value,
                markedByRole = role,
                markedById = id,
                markedByName = name
            )
            showToast("Marked all ${employees.size} employees Present (P) / सभी को उपस्थित मार्क किया")
        }
    }

    // === Employee Management (Max 10) ===
    fun saveEmployee(employee: Employee, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.saveEmployee(employee)
            if (res.isSuccess) {
                onResult(true, "Employee saved successfully / कर्मचारी सहेजा गया")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Error saving employee")
            }
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            repository.deleteEmployee(id)
            showToast("Employee deleted / कर्मचारी हटाया गया")
        }
    }

    // === Operator Management ===
    fun saveOperator(operator: Operator, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.saveOperator(operator)
            onResult(true, "Operator saved successfully / ऑपरेटर सहेजा गया")
        }
    }

    fun deleteOperator(operator: Operator) {
        viewModelScope.launch {
            repository.deleteOperator(operator)
            showToast("Operator deleted / ऑपरेटर हटाया गया")
        }
    }

    // === Reports & Exporting ===
    fun exportCsvReport(records: List<AttendanceRecord>, title: String) {
        viewModelScope.launch {
            val file = repository.exportAttendanceToCsv(records, title)
            if (file != null) {
                repository.shareCsvFile(file)
            } else {
                showToast("Failed to create Excel/CSV file")
            }
        }
    }

    fun printPdfReport(title: String, records: List<AttendanceRecord>, summaryInfo: String) {
        repository.printOrSavePdfReport(title, records, summaryInfo)
    }

    // === Backup & Restore ===
    fun createBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.createBackupJson()
            onReady(json)
        }
    }

    fun restoreBackup(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.restoreBackupJson(json)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Success")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to restore")
            }
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            showToast("Reset to sample loading yard data complete / नमूना डेटा लोड हो गया")
        }
    }
}
