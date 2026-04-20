package org.fkit.hrm.util.common;

import org.fkit.hrm.domain.User;

public class UserContextHolder {
	
	private static final ThreadLocal<User> userContext = new ThreadLocal<>();
	
	public static void setUser(User user) {
		userContext.set(user);
	}
	
	public static User getUser() {
		return userContext.get();
	}
	
	public static void clear() {
		userContext.remove();
	}
}
