package com.taskmanager.model.enums;

/**
 * Role defines the authorization level of a user in the system.
 * 
 * Spring Security uses roles to control access to different endpoints.
 * For example, only ADMIN users can delete other users' tasks,
 * while regular USER role members can only manage their own tasks.
 * 
 * In Spring Security, roles are typically prefixed with "ROLE_" internally,
 * but we define them without the prefix here for cleaner code.
 */
public enum Role {
    USER,   // Regular user - can manage their own tasks
    ADMIN   // Administrator - can manage all tasks and users
}
