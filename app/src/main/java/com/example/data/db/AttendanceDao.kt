package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.Operator
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    // === Employees ===
    @Query("SELECT * FROM employees ORDER BY id ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY id ASC")
    suspend fun getAllEmployeesSync(): List<Employee>

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): Employee?

    @Query("SELECT * FROM employees WHERE assignedOperatorId = :operatorId AND isActive = 1 ORDER BY id ASC")
    fun getEmployeesForOperator(operatorId: String): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("DELETE FROM employees")
    suspend fun clearEmployees()


    // === Operators ===
    @Query("SELECT * FROM operators ORDER BY id ASC")
    fun getAllOperators(): Flow<List<Operator>>

    @Query("SELECT * FROM operators ORDER BY id ASC")
    suspend fun getAllOperatorsSync(): List<Operator>

    @Query("SELECT * FROM operators WHERE id = :id LIMIT 1")
    suspend fun getOperatorById(id: String): Operator?

    @Query("SELECT * FROM operators WHERE username = :username AND isActive = 1 LIMIT 1")
    suspend fun getOperatorByUsername(username: String): Operator?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: Operator)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperators(operators: List<Operator>)

    @Update
    suspend fun updateOperator(operator: Operator)

    @Delete
    suspend fun deleteOperator(operator: Operator)

    @Query("DELETE FROM operators")
    suspend fun clearOperators()


    // === Attendance Records ===
    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY id DESC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceForDateSync(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, employeeId ASC")
    fun getAttendanceBetweenDates(startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE markedById = :operatorId ORDER BY date DESC, id DESC")
    fun getAttendanceMarkedBy(operatorId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, employeeId ASC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, employeeId ASC")
    suspend fun getAllAttendanceSync(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE employeeId = :employeeId AND date = :date")
    suspend fun deleteAttendanceForDate(employeeId: String, date: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAttendance()


    // === Settings ===
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)
}
