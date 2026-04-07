Feature: Tracking Service API

  Background:
    * url baseUrl

  Scenario: Get tracking history returns empty list for non-existent shipment
    * def shipmentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/tracking/' + shipmentId + '/history'
    When method GET
    Then status 200
    And match response == '#[]'
    And match response == []

  Scenario: Get current status returns 404 for non-existent shipment
    * def shipmentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/tracking/' + shipmentId + '/current'
    When method GET
    Then status 404
    And match response.code == 'NOT_FOUND'
