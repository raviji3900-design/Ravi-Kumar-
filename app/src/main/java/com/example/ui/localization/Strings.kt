package com.example.ui.localization

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    HI("hi", "हिन्दी"),
    DUAL("dual", "Dual / दोनों")
}

object AppStrings {
    fun t(key: String, lang: AppLanguage): String {
        val entry = dictionary[key] ?: return key
        return when (lang) {
            AppLanguage.EN -> entry.en
            AppLanguage.HI -> entry.hi
            AppLanguage.DUAL -> "${entry.hi} / ${entry.en}"
        }
    }

    private data class Trans(val en: String, val hi: String)

    private val dictionary = mapOf(
        "app_title" to Trans("Loading Works Attendance", "लोडिंग वर्क्स हज़िरी"),
        "app_subtitle" to Trans("Attendance Manager", "हज़िरी प्रबंधक"),
        "admin_login" to Trans("Admin Login", "एडमिन लॉगिन"),
        "operator_login" to Trans("Operator Login", "ऑपरेटर लॉगिन"),
        "username" to Trans("Username / ID", "उपयोगकर्ता नाम / आईडी"),
        "password" to Trans("Password", "पासवर्ड"),
        "login_button" to Trans("Sign In", "लॉगिन करें"),
        "logout" to Trans("Logout", "लॉग आउट"),
        "admin" to Trans("Admin", "एडमिन"),
        "operator" to Trans("Operator", "ऑपरेटर"),
        "dashboard" to Trans("Dashboard", "डैशबोर्ड"),
        "today_attendance" to Trans("Today's Attendance", "आज की हज़िरी"),
        "total_employees" to Trans("Total Workers", "कुल मजदूर / कर्मचारी"),
        "present" to Trans("Present", "उपस्थित"),
        "absent" to Trans("Absent", "अनुपस्थित"),
        "half_day" to Trans("Half Day", "आधा दिन"),
        "leave" to Trans("Leave", "छुट्टी"),
        "mark_attendance" to Trans("Mark Attendance", "हज़िरी लगाएं"),
        "mark_all_present" to Trans("Mark All Present", "सभी को उपस्थित करें"),
        "manage_employees" to Trans("Manage Employees", "कर्मचारी प्रबंधन"),
        "manage_operators" to Trans("Manage Operators", "ऑपरेटर प्रबंधन"),
        "reports" to Trans("Reports", "रिपोर्ट्स"),
        "calendar" to Trans("Calendar View", "कैलेंडर दृश्य"),
        "settings" to Trans("Settings & Backup", "सेटिंग्स और बैकअप"),
        "add_employee" to Trans("Add Employee", "नया कर्मचारी जोड़ें"),
        "edit_employee" to Trans("Edit Employee", "कर्मचारी विवरण बदलें"),
        "delete_employee" to Trans("Delete", "हटाएं"),
        "employee_name" to Trans("Employee Name", "कर्मचारी का नाम"),
        "employee_id" to Trans("Employee ID", "कर्मचारी आईडी"),
        "phone_number" to Trans("Phone Number", "फ़ोन नंबर"),
        "designation" to Trans("Designation / Role", "पद / काम"),
        "assigned_operator" to Trans("Assigned Operator", "नियुक्त ऑपरेटर"),
        "unassigned" to Trans("Unassigned / All", "कोई भी नहीं / सभी"),
        "save" to Trans("Save", "सुरक्षित करें"),
        "cancel" to Trans("Cancel", "रद्द करें"),
        "delete_confirm" to Trans("Are you sure you want to delete this?", "क्या आप वाकई इसे हटाना चाहते हैं?"),
        "limit_reached_title" to Trans("Limit Reached (Max 10)", "अधिकतम सीमा (अधिकतम 10 कर्मचारी)"),
        "limit_reached_msg" to Trans("You can manage up to 10 employees. Please edit or delete an existing employee.", "आप अधिकतम 10 कर्मचारियों का प्रबंधन कर सकते हैं।"),
        "duplicate_attendance" to Trans("Attendance Already Marked", "हज़िरी पहले से दर्ज है"),
        "duplicate_attendance_msg" to Trans("Attendance for this employee was already recorded today. Do you want to update it?", "इस कर्मचारी की हज़िरी आज पहले ही दर्ज हो चुकी है। क्या आप इसे बदलना चाहते हैं?"),
        "daily_report" to Trans("Daily Report", "दैनिक रिपोर्ट"),
        "weekly_report" to Trans("Weekly Report", "साप्ताहिक रिपोर्ट"),
        "monthly_report" to Trans("Monthly Report", "मासिक रिपोर्ट"),
        "employee_report" to Trans("Employee Report", "कर्मचारी-वार रिपोर्ट"),
        "operator_report" to Trans("Operator Report", "ऑपरेटर-वार रिपोर्ट"),
        "export_excel" to Trans("Export Excel (CSV)", "एक्सेल (CSV) डाउनलोड करें"),
        "export_pdf" to Trans("Print / PDF Report", "पीडीएफ (PDF) प्रिंट करें"),
        "share_report" to Trans("Share Report", "रिपोर्ट शेयर करें"),
        "backup_restore" to Trans("Backup & Restore Data", "डेटा बैकअप और रीस्टोर"),
        "export_backup" to Trans("Export Full Backup (JSON)", "पूरा बैकअप निकालें (JSON)"),
        "restore_backup" to Trans("Restore from Backup", "बैकअप से रीस्टोर करें"),
        "offline_status" to Trans("Offline Ready (Local Storage)", "ऑफलाइन तैयार (लोकल स्टोरेज)"),
        "sync_now" to Trans("Sync Data", "डेटा सिंक करें"),
        "sync_success" to Trans("All data saved safely locally & synchronized", "सभी डेटा सुरक्षित रूप से सहेजा गया"),
        "search_hint" to Trans("Search by name or ID...", "नाम या आईडी से खोजें..."),
        "assigned_workers_count" to Trans("Assigned Workers", "आवंटित मजदूर"),
        "my_dashboard" to Trans("Operator Dashboard", "ऑपरेटर डैशबोर्ड"),
        "my_activity_log" to Trans("My Marked Attendance", "मेरे द्वारा लगाई गई हज़िरी"),
        "operator_warning" to Trans("Operator restricted from admin settings", "ऑपरेटर एडमिन सेटिंग्स में नहीं जा सकता"),
        "add_operator" to Trans("Add Operator", "नया ऑपरेटर जोड़ें"),
        "shift" to Trans("Shift (e.g. Morning/Night)", "शिफ्ट (उदा. सुबह/रात)"),
        "enter_password" to Trans("Enter Password", "पासवर्ड दर्ज करें"),
        "change_admin_pass" to Trans("Change Admin Password", "एडमिन पासवर्ड बदलें"),
        "current_password" to Trans("Current Password", "वर्तमान पासवर्ड"),
        "new_password" to Trans("New Password", "नया पासवर्ड"),
        "update_password" to Trans("Update Password", "पासवर्ड अपडेट करें"),
        "attendance_rate" to Trans("Attendance Rate", "हज़िरी प्रतिशत"),
        "filter_date" to Trans("Select Date", "तारीख चुनें"),
        "status_updated" to Trans("Status updated successfully", "हज़िरी सफलतापूर्वक अपडेट की गई"),
        "no_records" to Trans("No attendance records found", "कोई हज़िरी रिकॉर्ड नहीं मिला"),
        "reset_sample_data" to Trans("Reset Sample Data", "नमूना डेटा रीसेट करें"),
        "p_label" to Trans("P - Present", "उपस्थित (P)"),
        "a_label" to Trans("A - Absent", "अनुपस्थित (A)"),
        "h_label" to Trans("H - Half Day", "आधा दिन (H)"),
        "l_label" to Trans("L - Leave", "छुट्टी (L)")
    )
}
