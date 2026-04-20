package org.fkit.hrm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fkit.hrm.dao.DeptDao;
import org.fkit.hrm.dao.DocumentDao;
import org.fkit.hrm.dao.EmployeeDao;
import org.fkit.hrm.dao.JobDao;
import org.fkit.hrm.dao.NoticeDao;
import org.fkit.hrm.dao.UserDao;
import org.fkit.hrm.domain.Dept;
import org.fkit.hrm.domain.Document;
import org.fkit.hrm.domain.Employee;
import org.fkit.hrm.domain.Job;
import org.fkit.hrm.domain.Notice;
import org.fkit.hrm.domain.User;
import org.fkit.hrm.service.HrmService;
import org.fkit.hrm.util.common.HrmConstants;
import org.fkit.hrm.util.common.RedisUtil;
import org.fkit.hrm.util.common.UserContextHolder;
import org.fkit.hrm.util.tag.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**   
 * @Description: 人事管理系统服务层接口实现类
 */
@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.DEFAULT)
@Service("hrmService")
public class HrmServiceImpl implements HrmService{

	/**
	 * 自动注入持久层Dao对象
	 * */
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DeptDao deptDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private NoticeDao noticeDao;
	
	@Autowired
	private DocumentDao documentDao;
	
	@Autowired
	private RedisUtil redisUtil;
	
	/**
	 * 获取当前登录用户
	 * 优先从Redis缓存获取，其次从UserContextHolder获取
	 * @return 当前用户对象
	 */
	private User getCurrentUser() {
		User currentUser = UserContextHolder.getUser();
		if (currentUser != null) {
			return currentUser;
		}
		return null;
	}
	
	/**
	 * 获取当前用户的角色
	 * @return 角色值，如果未登录或无角色则返回null
	 */
	private Integer getCurrentUserRole() {
		User currentUser = getCurrentUser();
		if (currentUser != null && currentUser.getRole() != null) {
			return currentUser.getRole();
		}
		return null;
	}
	
	/**
	 * 获取当前用户所在的部门ID
	 * 部门领导和普通员工需要通过employeeId查询Employee获取部门信息
	 * @return 部门ID，如果无法获取则返回null
	 */
	private Integer getCurrentUserDeptId() {
		User currentUser = getCurrentUser();
		if (currentUser == null || currentUser.getEmployeeId() == null) {
			return null;
		}
		Employee employee = employeeDao.selectById(currentUser.getEmployeeId());
		if (employee != null && employee.getDept() != null) {
			return employee.getDept().getId();
		}
		return null;
	}
	
	/**
	 * 检查是否是管理员
	 * @return true如果是管理员
	 */
	private boolean isAdmin() {
		Integer role = getCurrentUserRole();
		return role != null && role == HrmConstants.ROLE_ADMIN;
	}
	
	/**
	 * 检查是否是部门领导
	 * @return true如果是部门领导
	 */
	private boolean isDeptLeader() {
		Integer role = getCurrentUserRole();
		return role != null && role == HrmConstants.ROLE_DEPT_LEADER;
	}
	
	/**
	 * 检查是否是普通员工
	 * @return true如果是普通员工
	 */
	private boolean isEmployee() {
		Integer role = getCurrentUserRole();
		return role != null && role == HrmConstants.ROLE_EMPLOYEE;
	}
	
	/**
	 * 应用员工查询权限过滤
	 * 根据当前用户角色修改查询条件
	 * @param employee 查询条件
	 */
	private void applyEmployeePermissionFilter(Employee employee) {
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			return;
		}
		
		if (isAdmin()) {
			return;
		} else if (isDeptLeader()) {
			Integer deptId = getCurrentUserDeptId();
			if (deptId != null) {
				if (employee.getDept() == null) {
					employee.setDept(new Dept());
				}
				employee.getDept().setId(deptId);
			}
		} else if (isEmployee()) {
			Integer employeeId = currentUser.getEmployeeId();
			if (employeeId != null) {
				employee.setId(employeeId);
			}
		}
	}
	
	/**
	 * 检查单个员工查询权限
	 * @param employeeId 要查询的员工ID
	 * @return true如果有权限查询
	 */
	private boolean checkEmployeePermission(Integer employeeId) {
		if (employeeId == null) {
			return false;
		}
		
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			return false;
		}
		
		if (isAdmin()) {
			return true;
		} else if (isDeptLeader()) {
			Employee targetEmployee = employeeDao.selectById(employeeId);
			if (targetEmployee == null || targetEmployee.getDept() == null) {
				return false;
			}
			Integer currentDeptId = getCurrentUserDeptId();
			return currentDeptId != null && currentDeptId.equals(targetEmployee.getDept().getId());
		} else if (isEmployee()) {
			Integer currentEmployeeId = currentUser.getEmployeeId();
			return currentEmployeeId != null && currentEmployeeId.equals(employeeId);
		}
		
		return false;
	}
	
	/**
	 * 应用用户查询权限过滤
	 * 根据当前用户角色修改查询条件
	 * @param user 查询条件
	 */
	private void applyUserPermissionFilter(User user) {
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			return;
		}
		
		if (isAdmin()) {
			return;
		} else if (isDeptLeader()) {
			user.setId(null);
		} else if (isEmployee()) {
			user.setId(currentUser.getId());
		}
	}
	
	/**
	 * 检查单个用户查询权限
	 * @param userId 要查询的用户ID
	 * @return true如果有权限查询
	 */
	private boolean checkUserPermission(Integer userId) {
		if (userId == null) {
			return false;
		}
		
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			return false;
		}
		
		if (isAdmin()) {
			return true;
		} else if (isDeptLeader()) {
			User targetUser = userDao.selectById(userId);
			if (targetUser == null || targetUser.getEmployeeId() == null) {
				return false;
			}
			Employee targetEmployee = employeeDao.selectById(targetUser.getEmployeeId());
			if (targetEmployee == null || targetEmployee.getDept() == null) {
				return false;
			}
			Integer currentDeptId = getCurrentUserDeptId();
			return currentDeptId != null && currentDeptId.equals(targetEmployee.getDept().getId());
		} else if (isEmployee()) {
			return currentUser.getId() != null && currentUser.getId().equals(userId);
		}
		
		return false;
	}
	
	/*****************用户服务接口实现*************************************/
	/**
	 * HrmServiceImpl接口login方法实现
	 *  @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public User login(String loginname, String password) {
		User user = userDao.selectByLoginnameAndPassword(loginname, password);
		if (user != null) {
			try {
				String redisKey = "user:login:" + user.getId();
				redisUtil.set(redisKey, user, 1800);
			} catch (Exception e) {
				System.err.println("Redis缓存用户信息失败: " + e.getMessage());
			}
		}
		return user;
	}

	/**
	 * HrmServiceImpl接口findUser方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<User> findUser(User user,PageModel pageModel) {
		applyUserPermissionFilter(user);
		
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("user", user);
		int recordCount = userDao.count(params);
		pageModel.setRecordCount(recordCount);
		if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
		List<User> users = userDao.selectByPage(params);
		 
		return users;
	}
	
	/**
	 * HrmServiceImpl接口findUserById方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public User findUserById(Integer id) {
		if (!checkUserPermission(id)) {
			return null;
		}
		return userDao.selectById(id);
	}
	
	/**
	 * HrmServiceImpl接口removeUserById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeUserById(Integer id) {
		userDao.deleteById(id);
		
	}
	
	/**
	 * HrmServiceImpl接口addUser方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyUser(User user) {
		userDao.update(user);
		
	}
	
	/**
	 * HrmServiceImpl接口modifyUser方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addUser(User user) {
		userDao.save(user);
		
	}
	
	/*****************部门服务接口实现*************************************/
	@Transactional(readOnly=true)
	@Override
	public List<Dept> findAllDept() {
		
		return deptDao.selectAllDept();
	}
	
	/**
	 * HrmServiceImpl接口findDept方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<Dept> findDept(Dept dept,PageModel pageModel) {
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("dept", dept);
		int recordCount = deptDao.count(params);
		pageModel.setRecordCount(recordCount);
		
		if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
		
		List<Dept> depts = deptDao.selectByPage(params);
		 
		return depts;
	}
	
	/**
	 * HrmServiceImpl接口removeUserById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeDeptById(Integer id) {
		deptDao.deleteById(id);
		
	}

	/**
	 * HrmServiceImpl接口addDept方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addDept(Dept dept) {
		deptDao.save(dept);
		
	}
	
	/**
	 * HrmServiceImpl接口findDeptById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public Dept findDeptById(Integer id) {
		
		return deptDao.selectById(id);
	}
	
	/**
	 * HrmServiceImpl接口modifyDept方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyDept(Dept dept) {
		deptDao.update(dept);
		
	}
	/*****************员工服务接口实现*************************************/
	/**
	 * HrmService接口findEmployee方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<Employee> findEmployee(Employee employee,PageModel pageModel) {
		applyEmployeePermissionFilter(employee);
		
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("employee", employee);
		
		int recordCount = employeeDao.count(params);
	    pageModel.setRecordCount(recordCount);
	    
	    if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
	    List<Employee> employees = employeeDao.selectByPage(params);
	    return employees;
	}
	/**
	 * HrmService接口removeEmployeeById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeEmployeeById(Integer id) {
		employeeDao.deleteById(id);
		
	}
	/**
	 * HrmService接口findEmployeeById方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public Employee findEmployeeById(Integer id) {
		if (!checkEmployeePermission(id)) {
			return null;
		}
		return employeeDao.selectById(id);
	}
	
	/**
	 * HrmService接口addEmployee方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addEmployee(Employee employee) {
		employeeDao.save(employee);
		
	}
	
	/**
	 * HrmService接口modifyEmployee方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyEmployee(Employee employee) {
		employeeDao.update(employee);
	}
	
	/*****************职位接口实现*************************************/

	/**
	 * HrmService接口findAllJob方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<Job> findAllJob() {
		
		return jobDao.selectAllJob();
	}

	/**
	 * HrmService接口findJob方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<Job> findJob(Job job, PageModel pageModel) {
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("job", job);
		int recordCount = jobDao.count(params);
		pageModel.setRecordCount(recordCount);
		
		if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
		
		List<Job> jobs = jobDao.selectByPage(params);
		 
		return jobs;
	}
	
	/**
	 * HrmService接口removeJobById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeJobById(Integer id) {
		jobDao.deleteById(id);
		
	}
	
	/**
	 * HrmService接口addJob方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addJob(Job job) {
		jobDao.save(job);
		
	}
	
	/**
	 * HrmService接口findJobById方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public Job findJobById(Integer id) {
		
		return jobDao.selectById(id);
	}
	
	/**
	 * HrmService接口modifyJob方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyJob(Job job) {
		jobDao.update(job);
		
	}
	
	/*****************公告接口实现*************************************/
	@Transactional(readOnly=true)
	@Override
	public List<Notice> findNotice(Notice notice, PageModel pageModel) {
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("notice", notice);
		int recordCount = noticeDao.count(params);
		pageModel.setRecordCount(recordCount);
		
		if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
		
		List<Notice> notices = noticeDao.selectByPage(params);
		 
		return notices;
	}

	/**
	 * HrmService接口findNoticeById方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public Notice findNoticeById(Integer id) {
		
		return noticeDao.selectById(id);
	}

	/**
	 * HrmService接口removeNoticeById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeNoticeById(Integer id) {
		noticeDao.deleteById(id);
		
	}
	
	/**
	 * HrmService接口addNotice方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addNotice(Notice notice) {
		noticeDao.save(notice);
		
	}
	
	/**
	 * HrmService接口modifyNotice方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyNotice(Notice notice) {
		noticeDao.update(notice);
		
	}

	/*****************文件接口实现*************************************/

	/**
	 * HrmService接口findDocument方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public List<Document> findDocument(Document document, PageModel pageModel) {
		/** 当前需要分页的总数据条数  */
		Map<String,Object> params = new HashMap<>();
		params.put("document", document);
		int recordCount = documentDao.count(params);
		pageModel.setRecordCount(recordCount);
		
		if(recordCount > 0){
	        /** 开始分页查询数据：查询第几页的数据 */
		    params.put("pageModel", pageModel);
	    }
		
		List<Document> documents = documentDao.selectByPage(params);
		 
		return documents;
	}

	/**
	 * HrmService接口addDocument方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void addDocument(Document document) {
		documentDao.save(document);
		
	}
	/**
	 * HrmService接口removeDocumentById方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void removeDocumentById(Integer id) {
		documentDao.deleteById(id);
		
	}
	/**
	 * HrmService接口modifyDocument方法实现
	 * @see { HrmService }
	 * */
	@Override
	public void modifyDocument(Document document) {
		documentDao.update(document);
		
	}
	/**
	 * HrmService接口findDocumentById方法实现
	 * @see { HrmService }
	 * */
	@Transactional(readOnly=true)
	@Override
	public Document findDocumentById(Integer id) {
		
		return documentDao.selectById(id);
	}

	
}
