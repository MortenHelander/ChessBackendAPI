Feature: API testing with Rest Assured
  Scenario: Verify successful get request to retrieve user details
    Given I have the base-url for the API
    When I send a get request to "/users/1"
    Then I should recieve a 200 OK response
    And the information should match the one I registered with
    And no password information should be send