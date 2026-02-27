package com.rev.app.service;

import com.rev.app.dto.DashboardDTO;
import com.rev.app.entity.Employee;

public interface IDashboardService {

    DashboardDTO getDashboard(Employee currentUser);
}
