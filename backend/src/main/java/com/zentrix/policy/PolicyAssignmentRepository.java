package com.zentrix.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyAssignmentRepository extends JpaRepository<PolicyAssignment, Integer> {

    List<PolicyAssignment> findByPolicyCompanyId(Integer companyId);

    List<PolicyAssignment> findByDeviceIdAndPolicyType(Integer deviceId, PolicyType type);

    List<PolicyAssignment> findByGroupIdAndPolicyType(Integer groupId, PolicyType type);

    List<PolicyAssignment> findByPolicyId(Integer policyId);
}
