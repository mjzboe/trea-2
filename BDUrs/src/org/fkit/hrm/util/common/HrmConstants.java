package org.fkit.hrm.util.common;

public class HrmConstants {
	
	// 数据库表常量
	public static final String USERTABLE = "user_inf";
	public static final String DEPTTABLE = "dept_inf";
	public static final String JOBTABLE = "job_inf";
	public static final String EMPLOYEETABLE = "employee_inf";
	public static final String NOTICETABLE = "notice_inf";
	public static final String DOCUMENTTABLE = "document_inf";
	
	// 登录
	public static final String LOGIN = "loginForm";
	// 用户的session对象
	public static final String USER_SESSION = "user_session";
	
	// 默认每页4条数据
	public static int PAGE_DEFAULT_SIZE = 4;
	
	// 用户角色常量
	public static final int ROLE_ADMIN = 1;      // 管理员
	public static final int ROLE_DEPT_LEADER = 2; // 部门领导
	public static final int ROLE_EMPLOYEE = 3;    // 普通员工
	
	// Redis缓存key前缀
	public static final String REDIS_USER_LOGIN_PREFIX = "user:login:";
	
	
	
}
