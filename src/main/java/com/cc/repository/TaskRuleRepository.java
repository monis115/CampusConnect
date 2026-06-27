package com.cc.repository;

import com.cc.modals.TaskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRuleRepository extends JpaRepository<TaskRule, Long> {

    List<TaskRule> findByEnabledTrue();

    List<TaskRule> findByDepartmentAndEnabledTrue(String department);

    Optional<TaskRule> findByRuleCode(String ruleCode);
}
