package com.campus.common;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 *
 * @author campus
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户ID */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /** 获取当前用户角色 */
    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    /** 获取当前企业ID；企业HR返回所属公司ID，其他角色返回自身用户ID */
    public static Long getEnterpriseId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            return null;
        }
        return user.getEnterpriseId() == null ? user.getUserId() : user.getEnterpriseId();
    }

    /** 获取当前HR角色 */
    public static String getHrRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getHrRole();
    }

    /** 当前企业用户是否主管HR */
    public static boolean isSupervisorHr() {
        return "SUPERVISOR".equals(getHrRole());
    }

    /** 获取当前用户名 */
    public static String getUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
