Feature: Attendance routing

  Scenario Outline: Route attendance by subject
    Given the FlowPay teams are available
    When the customer informs the subject "<subject>"
    Then the attendance should be routed to "<team>"

    Examples:
      | subject                    | team        |
      | Problemas com cartao       | CARTOES     |
      | Contratacao de emprestimo  | EMPRESTIMOS |
      | Atualizacao cadastral      | OUTROS      |
