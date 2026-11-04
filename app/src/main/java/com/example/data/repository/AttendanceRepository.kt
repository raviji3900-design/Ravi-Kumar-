package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.db.AttendanceDao
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.Operator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val dao: AttendanceDao,
    private val context: Context
) {

    val allEmployees: Flow<List<Employee>> = dao.getAllEmployees()
    val allOperators: Flow<List<Operator>> = dao.getAllOperators()
    val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()

    fun getEmployeesForOperator(operatorId: String): Flow<List<Employee>> {
        return dao.getEmployeesForOperator(operatorId)
    }

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForDate(date)
    }

    fun getAttendanceForEmployee(empId: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForEmployee(empId)
    }

    fun getAttendanceBetweenDates(startDate: String, endDate: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceBetweenDates(startDate, endDate)
    }

    fun getAttendanceMarkedBy(operatorId: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceMarkedBy(operatorId)
    }

    // === Security & Authentication ===
    suspend fun verifyAdminPassword(password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val savedPassword = dao.getSetting("ADMIN_PASSWORD") ?: "admin123"
            password == savedPassword
        }
    }

    suspend fun updateAdminPassword(newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            dao.setSetting(AppSetting("ADMIN_PASSWORD", newPassword))
            true
        }
    }

    suspend fun authenticateOperator(username: String, pass: String): Operator? {
        return withContext(Dispatchers.IO) {
            val op = dao.getOperatorByUsername(username)
            if (op != null && op.password == pass) op else null
        }
    }

    // === Employee Management (Max 10) ===
    suspend fun canAddEmployee(): Boolean {
        return withContext(Dispatchers.IO) {
            dao.getEmployeeCount() < 10
        }
    }

    suspend fun getEmployeeCount(): Int {
        return withContext(Dispatchers.IO) {
            dao.getEmployeeCount()
        }
    }

    suspend fun saveEmployee(employee: Employee): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val existing = dao.getEmployeeById(employee.id)
            if (existing == null) {
                if (dao.getEmployeeCount() >= 10) {
                    return@withContext Result.failure(Exception("Maximum limit of 10 employees reached / अधिकतम 10 कर्मचारी सीमा"))
                }
                dao.insertEmployee(employee)
            } else {
                dao.updateEmployee(employee)
            }
            Result.success(Unit)
        }
    }

    suspend fun deleteEmployee(employeeId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteEmployeeById(employeeId)
        }
    }

    // === Operator Management ===
    suspend fun saveOperator(operator: Operator) {
        withContext(Dispatchers.IO) {
            val existing = dao.getOperatorById(operator.id)
            if (existing == null) {
                dao.insertOperator(operator)
            } else {
                dao.updateOperator(operator)
            }
        }
    }

    suspend fun deleteOperator(operator: Operator) {
        withContext(Dispatchers.IO) {
            dao.deleteOperator(operator)
        }
    }

    // === Attendance Marking ===
    suspend fun markAttendance(
        employeeId: String,
        employeeName: String,
        status: AttendanceStatus,
        date: String,
        markedByRole: String,
        markedById: String,
        markedByName: String,
        notes: String = ""
    ): AttendanceRecord {
        return withContext(Dispatchers.IO) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val record = AttendanceRecord(
                employeeId = employeeId,
                employeeName = employeeName,
                date = date,
                status = status.name,
                time = currentTime,
                markedByRole = markedByRole,
                markedById = markedById,
                markedByName = markedByName,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )

            dao.insertAttendance(record)
            record
        }
    }

    suspend fun markAllPresent(
        employees: List<Employee>,
        date: String,
        markedByRole: String,
        markedById: String,
        markedByName: String
    ) {
        withContext(Dispatchers.IO) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val records = employees.map { emp ->
                AttendanceRecord(
                    employeeId = emp.id,
                    employeeName = emp.name,
                    date = date,
                    status = AttendanceStatus.PRESENT.name,
                    time = currentTime,
                    markedByRole = markedByRole,
                    markedById = markedById,
                    markedByName = markedByName,
                    timestamp = System.currentTimeMillis()
                )
            }
            dao.insertAttendanceList(records)
        }
    }

    // === Reports & Export ===
    suspend fun exportAttendanceToCsv(
        records: List<AttendanceRecord>,
        reportTitle: String = "Attendance_Report"
    ): File? {
        return withContext(Dispatchers.IO) {
            try {
                val reportsDir = File(context.cacheDir, "reports")
                if (!reportsDir.exists()) reportsDir.mkdirs()

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(reportsDir, "${reportTitle}_$timeStamp.csv")

                val csvHeader = "Date,Employee ID,Employee Name,Status,Time,Marked By Role,Marked By Name,Notes\n"
                val stringBuilder = StringBuilder(csvHeader)

                for (r in records) {
                    val statusObj = AttendanceStatus.fromCode(r.status)
                    val statusText = "${statusObj.labelEn} (${statusObj.code})"
                    val escapedName = "\"${r.employeeName.replace("\"", "\"\"")}\""
                    val escapedNotes = "\"${r.notes.replace("\"", "\"\"")}\""
                    val escapedMarkedBy = "\"${r.markedByName.replace("\"", "\"\"")}\""

                    stringBuilder.append("${r.date},${r.employeeId},$escapedName,$statusText,${r.time},${r.markedByRole},$escapedMarkedBy,$escapedNotes\n")
                }

                FileOutputStream(file).use { out ->
                    out.write(stringBuilder.toString().toByteArray(Charsets.UTF_8))
                }
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun shareCsvFile(file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Loading Works Attendance Report")
                putExtra(Intent.EXTRA_TEXT, "Attendance Report exported from Loading Works Attendance Manager.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Excel / CSV Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printOrSavePdfReport(
        title: String,
        records: List<AttendanceRecord>,
        summaryInfo: String
    ) {
        try {
            val htmlContent = buildReportHtml(title, records, summaryInfo)
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter(title)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("att_print", "Loading Works", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager?.print(title, printAdapter, printAttributes)
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildReportHtml(
        title: String,
        records: List<AttendanceRecord>,
        summaryInfo: String
    ): String {
        val rows = StringBuilder()
        for (r in records) {
            val status = AttendanceStatus.fromCode(r.status)
            val badgeColor = when (status) {
                AttendanceStatus.PRESENT -> "#2E7D32"
                AttendanceStatus.ABSENT -> "#D32F2F"
                AttendanceStatus.HALF_DAY -> "#ED6C02"
                AttendanceStatus.LEAVE -> "#7B1FA2"
            }
            rows.append(
                """
                <tr>
                    <td>${r.date}</td>
                    <td><strong>${r.employeeId}</strong></td>
                    <td>${r.employeeName}</td>
                    <td><span style="color:white;background:$badgeColor;padding:3px 8px;border-radius:4px;font-weight:bold;">${status.labelEn} (${status.code})</span></td>
                    <td>${r.time}</td>
                    <td>${r.markedByName} (${r.markedByRole})</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>$title</title>
            <style>
                body { font-family: sans-serif; margin: 20px; color: #1e293b; }
                h1 { color: #0D47A1; margin-bottom: 4px; font-size: 22px; }
                .subtitle { color: #64748b; font-size: 14px; margin-bottom: 16px; }
                .summary { background: #f1f5f9; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 13px; line-height: 1.5; border-left: 4px solid #1976D2; }
                table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 12px; }
                th { background: #0D47A1; color: white; text-align: left; padding: 8px; font-size: 12px; }
                td { padding: 8px; border-bottom: 1px solid #e2e8f0; }
                tr:nth-child(even) { background: #f8fafc; }
                .footer { margin-top: 30px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <h1>Loading Works Attendance Manager</h1>
            <div class="subtitle">$title • Generated on ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}</div>
            <div class="summary">$summaryInfo</div>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>ID</th>
                        <th>Employee Name</th>
                        <th>Status</th>
                        <th>Time</th>
                        <th>Marked By</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>
            <div class="footer">
                Loading Works Attendance Management System • Official Record
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // === Backup & Restore (JSON) ===
    suspend fun createBackupJson(): String {
        return withContext(Dispatchers.IO) {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())
            root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Employees
            val empArray = JSONArray()
            val employees = dao.getAllEmployeesSync()
            for (e in employees) {
                val obj = JSONObject()
                obj.put("id", e.id)
                obj.put("name", e.name)
                obj.put("phone", e.phone)
                obj.put("designation", e.designation)
                obj.put("assignedOperatorId", e.assignedOperatorId)
                obj.put("isActive", e.isActive)
                empArray.put(obj)
            }
            root.put("employees", empArray)

            // Operators
            val opArray = JSONArray()
            val operators = dao.getAllOperatorsSync()
            for (op in operators) {
                val obj = JSONObject()
                obj.put("id", op.id)
                obj.put("name", op.name)
                obj.put("username", op.username)
                obj.put("password", op.password)
                obj.put("phone", op.phone)
                obj.put("shift", op.shift)
                obj.put("isActive", op.isActive)
                opArray.put(obj)
            }
            root.put("operators", opArray)

            // Attendance
            val attArray = JSONArray()
            val attendanceList = dao.getAllAttendanceSync()
            for (a in attendanceList) {
                val obj = JSONObject()
                obj.put("employeeId", a.employeeId)
                obj.put("employeeName", a.employeeName)
                obj.put("date", a.date)
                obj.put("status", a.status)
                obj.put("time", a.time)
                obj.put("markedByRole", a.markedByRole)
                obj.put("markedById", a.markedById)
                obj.put("markedByName", a.markedByName)
                obj.put("notes", a.notes)
                obj.put("timestamp", a.timestamp)
                attArray.put(obj)
            }
            root.put("attendance", attArray)

            root.toString(2)
        }
    }

    suspend fun restoreBackupJson(jsonString: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(jsonString)
                val empArray = root.optJSONArray("employees")
                val opArray = root.optJSONArray("operators")
                val attArray = root.optJSONArray("attendance")

                if (empArray != null) {
                    val restoredEmployees = mutableListOf<Employee>()
                    for (i in 0 until minOf(empArray.length(), 10)) {
                        val obj = empArray.getJSONObject(i)
                        restoredEmployees.add(
                            Employee(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                phone = obj.optString("phone", ""),
                                designation = obj.optString("designation", "Loading Worker"),
                                assignedOperatorId = obj.optString("assignedOperatorId", ""),
                                isActive = obj.optBoolean("isActive", true)
                            )
                        )
                    }
                    dao.clearEmployees()
                    dao.insertEmployees(restoredEmployees)
                }

                if (opArray != null) {
                    val restoredOps = mutableListOf<Operator>()
                    for (i in 0 until opArray.length()) {
                        val obj = opArray.getJSONObject(i)
                        restoredOps.add(
                            Operator(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                username = obj.getString("username"),
                                password = obj.getString("password"),
                                phone = obj.optString("phone", ""),
                                shift = obj.optString("shift", "General Shift"),
                                isActive = obj.optBoolean("isActive", true)
                            )
                        )
                    }
                    dao.clearOperators()
                    dao.insertOperators(restoredOps)
                }

                if (attArray != null) {
                    val restoredAtt = mutableListOf<AttendanceRecord>()
                    for (i in 0 until attArray.length()) {
                        val obj = attArray.getJSONObject(i)
                        restoredAtt.add(
                            AttendanceRecord(
                                employeeId = obj.getString("employeeId"),
                                employeeName = obj.getString("employeeName"),
                                date = obj.getString("date"),
                                status = obj.getString("status"),
                                time = obj.optString("time", "09:00 AM"),
                                markedByRole = obj.optString("markedByRole", "ADMIN"),
                                markedById = obj.optString("markedById", "ADMIN"),
                                markedByName = obj.optString("markedByName", "Admin"),
                                notes = obj.optString("notes", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                    dao.clearAttendance()
                    dao.insertAttendanceList(restoredAtt)
                }

                Result.success("Restored successfully / सफलतापूर्वक पुनर्स्थापित किया गया")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resetToSampleData() {
        withContext(Dispatchers.IO) {
            dao.clearAttendance()
            dao.clearEmployees()
            dao.clearOperators()
            AppDatabase.populateInitialData(dao)
        }
    }
}
