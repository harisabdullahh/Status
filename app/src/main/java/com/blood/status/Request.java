package com.blood.status;

public class Request {
    public static String get_status_lan = "http://172.16.100.111:8037/api/Employee/GetEmployeeDashboardStatusById?EmpId=";
    public static String get_status_wan = "http://103.93.216.26:8037/api/Employee/GetEmployeeDashboardStatusById?EmpId=";
    public static String post_mark_attendance_lan = "http://172.16.100.111:8037/api/Employee/MarkAttendance?EmpId=";
    public static String post_mark_attendance_wan = "http://103.93.216.26:8037/api/Employee/MarkAttendance?EmpId=";
    public static String emp_login_lan = "http://172.16.100.111:8037/api/Employee/EmployeeLogin";
    public static String emp_login_wan = "http://103.93.216.26:8037/api/Employee/EmployeeLogin";
    public static String get_calender_lan = "http://172.16.100.111:8037/api/Employee/CalenderAPI?EmpId=";
    public static String get_emp_info = "https://raw.githubusercontent.com/harisabdullahh/script/main/empInfo.json";
    public static String calender_date_jan = "&selectedDate=2024-01-10%2009:20:22.913";
    public static String emp_id = "";
    public static String device_id = "";
    public static String outputDateFormat = "dd MMMM yyyy";
    public static String outputTimeFormat = "h:mm a";
    public static String use_wan_text = "";
    public static String status_result = "";
    public static String status_date = "";
    public static String status_time = "";
    public static String status_ontime = "";
    public static String status_late = "";
    public static String status_absent = "";
    public static String status_leaveAssign = "";
    public static String status_leaveAvailable = "";
    public static boolean debug = true;

}
