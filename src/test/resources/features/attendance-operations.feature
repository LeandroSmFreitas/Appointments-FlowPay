Feature: Attendance operations

  Scenario: Assign attendance immediately when an agent has capacity
    Given an online agent from team "CARTOES" with 1 active attendance
    When a new attendance is distributed to team "CARTOES"
    Then the attendance status should be "IN_PROGRESS"
    And the attendance should be assigned to the agent
    And the agent active count should be 2

  Scenario: Keep attendance waiting when no agent has capacity
    Given no agent has capacity in team "CARTOES"
    When a new attendance is distributed to team "CARTOES"
    Then the attendance status should be "WAITING"
    And the attendance should not have an assigned agent

  Scenario: Never exceed three active attendances per agent
    Given an online agent from team "CARTOES" with 3 active attendances
    When a new attendance is distributed to team "CARTOES"
    Then the attendance status should be "WAITING"
    And the agent active count should be 3

  Scenario: Finish an attendance and distribute the next waiting attendance
    Given an in progress attendance assigned to an agent with 3 active attendances
    And there is a waiting attendance for the same team
    When the in progress attendance is finished
    Then the finished attendance status should be "FINISHED"
    And the waiting attendance should become "IN_PROGRESS"
    And the agent active count should be 3

  Scenario: Cancel an in progress attendance and distribute the next waiting attendance
    Given an in progress attendance assigned to an agent with 3 active attendances
    And there is a waiting attendance for the same team
    When the in progress attendance is cancelled
    Then the finished attendance status should be "CANCELLED"
    And the waiting attendance should become "IN_PROGRESS"
    And the agent active count should be 3

  Scenario: Create an online agent and consume waiting attendances from the team
    Given there are 2 waiting attendances for team "CARTOES"
    When a new agent is created for team "CARTOES"
    Then the created agent should be "ONLINE"
    And the created agent active count should be 2
    And 2 waiting attendances should have been distributed to the created agent

  Scenario: Bring an agent online and consume waiting attendances from the team
    Given an offline agent from team "CARTOES" with 0 active attendances
    And there are 3 waiting attendances for team "CARTOES"
    When the agent status is changed to "ONLINE"
    Then the created agent should be "ONLINE"
    And the created agent active count should be 3
    And 3 waiting attendances should have been distributed to the created agent
