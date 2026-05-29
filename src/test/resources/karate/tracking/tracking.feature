Feature: Tracking Service API

  Background:
    * url baseUrl

  Scenario: Get tracking history returns empty list for non-existent shipment
    * def shipmentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/tracking/' + shipmentId + '/history'
    When method GET
    Then status 200
    And match response.content == '#[]'
    And match response.empty == true

  Scenario: Get current status returns 404 for non-existent shipment
    * def shipmentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/tracking/' + shipmentId + '/current'
    When method GET
    Then status 404
    And match response.code == 'SHIPMENT_NOT_FOUND'

  Scenario: Event types catalog returns all types with target status
    Given path '/api/v1/tracking/eventTypes'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[0].name == '#string'
    And match response[0].targetStatus == '#string'

  Scenario: Register event with invalid event type returns 400
    * def shipmentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/tracking/' + shipmentId + '/events'
    And request { eventType: 'TIPO_INEXISTENTE', location: 'Hub', occurredAt: '2026-01-01T00:00:00' }
    When method POST
    Then status 400
