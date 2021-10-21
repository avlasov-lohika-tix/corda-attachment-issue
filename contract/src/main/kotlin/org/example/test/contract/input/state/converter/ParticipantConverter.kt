package org.example.test.contract.input.state.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ParticipantConverter : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): List<String> {
        return objectMapper.readValue(dbData, Array<String>::class.java).toList()
    }
}

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

