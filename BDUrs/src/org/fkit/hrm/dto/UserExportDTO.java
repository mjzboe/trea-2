package org.fkit.hrm.dto;

import java.io.Serializable;

public class UserExportDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String deptName;
	private String jobName;
	private String roleName;
	private Integer deptId;
	
	public UserExportDTO() {
		super();
	}
	
	public UserExportDTO(String username, String deptName, String jobName, String roleName, Integer deptId) {
		super();
		this.username = username;
		this.deptName = deptName;
		this.jobName = jobName;
		this.roleName = roleName;
		this.deptId = deptId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	@Override
	public String toString() {
		return "UserExportDTO [username=" + username + ", deptName=" + deptName + ", jobName=" + jobName
				+ ", roleName=" + roleName + ", deptId=" + deptId + "]";
	}
}
