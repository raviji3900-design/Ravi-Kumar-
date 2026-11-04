package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.Operator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        Employee::class,
        Operator::class,
        AttendanceRecord::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loading_works_attendance.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.attendanceDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AttendanceDao) {
            // Default admin password
            dao.setSetting(AppSetting("ADMIN_PASSWORD", "admin123"))

            // Sample Operators
            val operators = listOf(
                Operator(
                    id = "OP-01",
                    name = "Rajesh Sharma",
                    username = "operator1",
                    password = "123",
                    phone = "+91 98765 43210",
                    shift = "Morning Shift (सुबह की शिफ्ट)"
                ),
                Operator(
                    id = "OP-02",
                    name = "Manoj Singh",
                    username = "operator2",
                    password = "123",
                    phone = "+91 98765 43211",
                    shift = "Evening Shift (शाम की शिफ्ट)"
                )
            )
            dao.insertOperators(operators)

            // Sample Employees (6 initial loading workers, well within 10 limit)
            val employees = listOf(
                Employee("LW-101", "Ramesh Yadav", "+91 98765 00101", "Heavy Loading Specialist", "OP-01"),
                Employee("LW-102", "Suresh Kumar", "+91 98765 00102", "Forklift Driver", "OP-01"),
                Employee("LW-103", "Amit Verma", "+91 98765 00103", "Truck Loader", "OP-01"),
                Employee("LW-104", "Vikram Patel", "+91 98765 00104", "Warehouse Stacker", "OP-02"),
                Employee("LW-105", "Dinesh Rawat", "+91 98765 00105", "Loading Supervisor Helper", "OP-02"),
                Employee("LW-106", "Sunil Gujjar", "+91 98765 00106", "Pallet Handler", "OP-02")
            )
            dao.insertEmployees(employees)

            // Seed attendance for today and yesterday
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val todayStr = dateFormat.format(calendar.time)

            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(calendar.time)

            // Yesterday records
            val initialAttendance = mutableListOf<AttendanceRecord>()
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-101", employeeName = "Ramesh Yadav", date = yesterdayStr,
                status = AttendanceStatus.PRESENT.name, time = "08:45 AM", markedByRole = "OPERATOR",
                markedById = "OP-01", markedByName = "Rajesh Sharma"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-102", employeeName = "Suresh Kumar", date = yesterdayStr,
                status = AttendanceStatus.PRESENT.name, time = "08:50 AM", markedByRole = "OPERATOR",
                markedById = "OP-01", markedByName = "Rajesh Sharma"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-103", employeeName = "Amit Verma", date = yesterdayStr,
                status = AttendanceStatus.HALF_DAY.name, time = "09:10 AM", markedByRole = "OPERATOR",
                markedById = "OP-01", markedByName = "Rajesh Sharma"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-104", employeeName = "Vikram Patel", date = yesterdayStr,
                status = AttendanceStatus.PRESENT.name, time = "02:15 PM", markedByRole = "OPERATOR",
                markedById = "OP-02", markedByName = "Manoj Singh"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-105", employeeName = "Dinesh Rawat", date = yesterdayStr,
                status = AttendanceStatus.ABSENT.name, time = "02:30 PM", markedByRole = "OPERATOR",
                markedById = "OP-02", markedByName = "Manoj Singh"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-106", employeeName = "Sunil Gujjar", date = yesterdayStr,
                status = AttendanceStatus.LEAVE.name, time = "02:35 PM", markedByRole = "ADMIN",
                markedById = "ADMIN", markedByName = "Admin"
            ))

            // Today records (partial for realism, so user can mark the rest!)
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-101", employeeName = "Ramesh Yadav", date = todayStr,
                status = AttendanceStatus.PRESENT.name, time = "08:40 AM", markedByRole = "OPERATOR",
                markedById = "OP-01", markedByName = "Rajesh Sharma"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-102", employeeName = "Suresh Kumar", date = todayStr,
                status = AttendanceStatus.PRESENT.name, time = "08:42 AM", markedByRole = "OPERATOR",
                markedById = "OP-01", markedByName = "Rajesh Sharma"
            ))
            initialAttendance.add(AttendanceRecord(
                employeeId = "LW-103", employeeName = "Amit Verma", date = todayStr,
                status = AttendanceStatus.LEAVE.name, time = "09:00 AM", markedByRole = "ADMIN",
                markedById = "ADMIN", markedByName = "Admin", notes = "Approved Medical Leave"
            ))

            dao.insertAttendanceList(initialAttendance)
        }
    }
}
