package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.ServerUserRole;

public record ServerRoleDto (String email, ServerUserRole.Role role) {

}
