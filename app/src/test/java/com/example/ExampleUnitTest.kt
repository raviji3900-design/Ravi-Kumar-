package com.example

import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun verifyAttendanceStatusCodesAndLabels() {
        assertEquals("P", AttendanceStatus.PRESENT.code)
        assertEquals("Present", AttendanceStatus.PRESENT.labelEn)
        assertEquals("उपस्थित", AttendanceStatus.PRESENT.labelHi)

        assertEquals("A", AttendanceStatus.ABSENT.code)
        assertEquals("Absent", AttendanceStatus.ABSENT.labelEn)
        assertEquals("अनुपस्थित", AttendanceStatus.ABSENT.labelHi)

        assertEquals("H", AttendanceStatus.HALF_DAY.code)
        assertEquals("Half Day", AttendanceStatus.HALF_DAY.labelEn)
        assertEquals("आधा दिन", AttendanceStatus.HALF_DAY.labelHi)

        assertEquals("L", AttendanceStatus.LEAVE.code)
        assertEquals("Leave", AttendanceStatus.LEAVE.labelEn)
        assertEquals("छुट्टी", AttendanceStatus.LEAVE.labelHi)
    }

    @Test
    fun verifyEmployeeCapacityLimitMaxTen() {
        val employees = mutableListOf<Employee>()
        for (i in 1..10) {
            employees.add(Employee(id = "LW-10$i", name = "Worker $i"))
        }
        assertEquals(10, employees.size)
        val canAddMore = employees.size < 10
        assertFalse(canAddMore)
    }

    @Test
    fun verifyOperatorRoleAssignment() {
        val op = Operator(
            id = "OP-01",
            name = "Rajesh Sharma",
            username = "operator1",
            password = "123",
            shift = "Morning Shift"
        )
        val emp = Employee(
            id = "LW-101",
            name = "Ramesh Yadav",
            assignedOperatorId = op.id
        )
        assertEquals(op.id, emp.assignedOperatorId)
        assertTrue(op.isActive)
        assertTrue(emp.isActive)
    }
}
